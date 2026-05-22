package com.ecommerce.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    private Integer id;
    private String usuarioNombre;
    private String usuarioEmail;
    private String metodoPagoNombre;
    private String estado;
    private BigDecimal total;
    private BigDecimal descuentoAplicado;
    private String codigoUsado;
    private LocalDateTime fechaPedido;
    private List<DetallePedidoDTO> detalles;
}
