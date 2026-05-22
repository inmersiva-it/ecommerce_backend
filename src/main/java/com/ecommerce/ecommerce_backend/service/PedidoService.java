package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.DetallePedidoDTO;
import com.ecommerce.ecommerce_backend.dto.PedidoDTO;
import com.ecommerce.ecommerce_backend.dto.PedidoSaveDTO;
import com.ecommerce.ecommerce_backend.entity.*;
import com.ecommerce.ecommerce_backend.exception.BadRequestException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.repository.MetodoPagoRepository;
import com.ecommerce.ecommerce_backend.repository.PedidoRepository;
import com.ecommerce.ecommerce_backend.repository.ProductoRepository;
import com.ecommerce.ecommerce_backend.repository.UsuarioRepository;
import com.ecommerce.ecommerce_backend.repository.PromocionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PromocionRepository promocionRepository;

    @Transactional
    public PedidoDTO crearPedido(String userEmail, PedidoSaveDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));

        MetodoPago metodoPago = metodoPagoRepository.findById(dto.getMetodoPagoId())
                .orElseThrow(() -> new ResourceNotFoundException("Método de pago no encontrado con ID: " + dto.getMetodoPagoId()));

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BadRequestException("El pedido debe contener al menos un producto");
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setMetodoPago(metodoPago);
        pedido.setEstado("Pendiente");
        pedido.setFechaPedido(LocalDateTime.now());

        // Validate promo code if provided
        Promocion promocion = null;
        BigDecimal descuento = BigDecimal.ZERO;
        String codigoUsado = null;
        if (dto.getCodigoPromocion() != null && !dto.getCodigoPromocion().trim().isEmpty()) {
            String codigo = dto.getCodigoPromocion().trim().toUpperCase();
            promocion = promocionRepository.findByCodigo(codigo)
                    .orElseThrow(() -> new ResourceNotFoundException("Cupón de descuento no encontrado: " + codigo));
            if (!promocion.getActivo()) {
                throw new BadRequestException("El cupón de descuento no está activo: " + codigo);
            }
            if (promocion.getFechaVencimiento().isBefore(LocalDate.now())) {
                throw new BadRequestException("El cupón de descuento ha vencido: " + codigo);
            }
            codigoUsado = codigo;
        }

        BigDecimal total = BigDecimal.ZERO;
        List<DetallePedido> detalles = new ArrayList<>();

        for (PedidoSaveDTO.ItemPedidoSave item : dto.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new BadRequestException("Stock insuficiente para el producto: " + producto.getNombre() + " (Disponibles: " + producto.getStock() + ")");
            }

            // Deduct stock
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());

            detalles.add(detalle);

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);
        }

        if (promocion != null) {
            BigDecimal porcentaje = BigDecimal.valueOf(promocion.getPorcentajeDescuento());
            descuento = total.multiply(porcentaje).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            total = total.subtract(descuento);
            if (total.compareTo(BigDecimal.ZERO) < 0) {
                total = BigDecimal.ZERO;
            }
            pedido.setPromocion(promocion);
            pedido.setDescuentoAplicado(descuento);
            pedido.setCodigoUsado(codigoUsado);
        } else {
            pedido.setDescuentoAplicado(BigDecimal.ZERO);
        }

        pedido.setDetalles(detalles);
        pedido.setTotal(total);

        pedido = pedidoRepository.save(pedido);
        return mapToDTO(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO> obtenerPedidosPorUsuario(String userEmail) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));

        return pedidoRepository.findByUsuarioIdOrderByFechaPedidoDesc(usuario.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO> obtenerTodosLosPedidos() {
        return pedidoRepository.findAllByOrderByFechaPedidoDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PedidoDTO actualizarEstado(Integer pedidoId, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + pedidoId));

        // Validation for order statuses: Pendiente, Pagado, Enviado, Entregado, Cancelado
        pedido.setEstado(nuevoEstado);
        pedido = pedidoRepository.save(pedido);
        return mapToDTO(pedido);
    }

    @Transactional
    public void eliminarPedido(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));
        pedidoRepository.delete(pedido);
    }

    private PedidoDTO mapToDTO(Pedido p) {
        List<DetallePedidoDTO> detallesDTO = p.getDetalles().stream().map(d ->
                new DetallePedidoDTO(
                        d.getId(),
                        d.getProducto().getId(),
                        d.getProducto().getNombre(),
                        d.getCantidad(),
                        d.getPrecioUnitario()
                )
        ).collect(Collectors.toList());

        return new PedidoDTO(
                p.getId(),
                p.getUsuario().getNombre(),
                p.getUsuario().getEmail(),
                p.getMetodoPago().getNombre(),
                p.getEstado(),
                p.getTotal(),
                p.getDescuentoAplicado(),
                p.getCodigoUsado(),
                p.getFechaPedido(),
                detallesDTO
        );
    }
}
