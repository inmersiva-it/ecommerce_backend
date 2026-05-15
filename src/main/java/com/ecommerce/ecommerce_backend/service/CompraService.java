package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.CompraRequest;
import com.ecommerce.ecommerce_backend.dto.CompraResponseDTO;
import com.ecommerce.ecommerce_backend.dto.DetalleCompraResponseDTO;
import com.ecommerce.ecommerce_backend.entity.Compra;
import com.ecommerce.ecommerce_backend.entity.DetalleCompra;
import com.ecommerce.ecommerce_backend.entity.Producto;
import com.ecommerce.ecommerce_backend.entity.Usuario;
import com.ecommerce.ecommerce_backend.repository.CompraRepository;
import com.ecommerce.ecommerce_backend.repository.ProductoRepository;
import com.ecommerce.ecommerce_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PromocionService promocionService;

    @Transactional
    public void realizarCompra(String email, List<CompraRequest.ItemCompra> items, String codigoPromocion) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setFecha(LocalDateTime.now());
        
        List<DetalleCompra> detalles = new ArrayList<>();
        double totalCompra = 0.0;

        for (CompraRequest.ItemCompra item : items) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            // Descontar stock
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            DetalleCompra detalle = new DetalleCompra();
            detalle.setCompra(compra);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio().doubleValue());
            
            double subtotal = producto.getPrecio().doubleValue() * item.getCantidad();
            detalle.setSubtotal(subtotal);
            
            detalles.add(detalle);
            totalCompra += subtotal;
        }

        // Aplicar descuento si hay cupón
        double descuento = 0.0;
        String codigoUsado = null;
        if (codigoPromocion != null && !codigoPromocion.isBlank()) {
            int porcentaje = promocionService.validarCodigo(codigoPromocion);
            descuento = totalCompra * porcentaje / 100.0;
            totalCompra = totalCompra - descuento;
            codigoUsado = codigoPromocion.toUpperCase().trim();
        }

        compra.setDetalles(detalles);
        compra.setTotal(totalCompra);
        compra.setDescuentoAplicado(descuento);
        compra.setCodigoUsado(codigoUsado);

        compraRepository.save(compra);
    }

    public List<CompraResponseDTO> obtenerMisCompras(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        return compraRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId())
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<CompraResponseDTO> obtenerTodasLasCompras() {
        return compraRepository.findAllByOrderByFechaDesc()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private CompraResponseDTO mapToDTO(Compra compra) {
        CompraResponseDTO dto = new CompraResponseDTO();
        dto.setId(compra.getId());
        dto.setUsuarioNombre(compra.getUsuario().getNombre());
        dto.setUsuarioEmail(compra.getUsuario().getEmail());
        dto.setFecha(compra.getFecha());
        dto.setTotal(compra.getTotal());
        dto.setDescuentoAplicado(compra.getDescuentoAplicado() != null ? compra.getDescuentoAplicado() : 0.0);
        dto.setCodigoUsado(compra.getCodigoUsado());

        List<DetalleCompraResponseDTO> detallesDTO = compra.getDetalles().stream().map(d -> {
            DetalleCompraResponseDTO dDto = new DetalleCompraResponseDTO();
            dDto.setId(d.getId());
            dDto.setProductoNombre(d.getProducto().getNombre());
            dDto.setCantidad(d.getCantidad());
            dDto.setPrecioUnitario(d.getPrecioUnitario());
            dDto.setSubtotal(d.getSubtotal());
            return dDto;
        }).collect(Collectors.toList());

        dto.setDetalles(detallesDTO);
        return dto;
    }

    @Transactional
    public void eliminarCompra(Long id) {
        if (!compraRepository.existsById(id)) {
            throw new RuntimeException("Compra no encontrada: " + id);
        }
        compraRepository.deleteById(id);
    }
}

