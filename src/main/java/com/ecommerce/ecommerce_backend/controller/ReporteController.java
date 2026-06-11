package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.ReporteDTO;
import com.ecommerce.ecommerce_backend.entity.Usuario;
import com.ecommerce.ecommerce_backend.repository.PedidoRepository;
import com.ecommerce.ecommerce_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<?> obtenerReporteDashboard() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty() || usuarioOpt.get().getRol() == null ||
                !"Administrador".equalsIgnoreCase(usuarioOpt.get().getRol().getNombre())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Se requiere rol de Administrador");
        }

        List<Object[]> dbVentas = pedidoRepository.getSalesByMonth();
        List<ReporteDTO.VentasMes> ventasPorMes = dbVentas.stream()
                .map(obj -> new ReporteDTO.VentasMes(
                        obj[0] != null ? obj[0].toString() : "",
                        obj[1] != null ? new BigDecimal(obj[1].toString()) : BigDecimal.ZERO
                ))
                .collect(Collectors.toList());

        List<Object[]> dbProducts = pedidoRepository.getTopSellingProducts();
        List<ReporteDTO.ProductoVendido> topProductos = dbProducts.stream()
                .map(obj -> new ReporteDTO.ProductoVendido(
                        obj[0] != null ? obj[0].toString() : "",
                        obj[1] != null ? Long.valueOf(obj[1].toString()) : 0L
                ))
                .collect(Collectors.toList());

        List<Object[]> dbStatus = pedidoRepository.getOrderStatuses();
        List<ReporteDTO.EstadoPedido> estadoPedidos = dbStatus.stream()
                .map(obj -> new ReporteDTO.EstadoPedido(
                        obj[0] != null ? obj[0].toString() : "",
                        obj[1] != null ? Long.valueOf(obj[1].toString()) : 0L
                ))
                .collect(Collectors.toList());

        ReporteDTO reporte = new ReporteDTO(ventasPorMes, topProductos, estadoPedidos);
        return ResponseEntity.ok(reporte);
    }
}
