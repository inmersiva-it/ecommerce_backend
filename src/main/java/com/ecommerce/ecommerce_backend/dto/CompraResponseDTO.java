package com.ecommerce.ecommerce_backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompraResponseDTO {
    private Long id;
    private String usuarioNombre;
    private String usuarioEmail;
    private LocalDateTime fecha;
    private Double total;
    private Double descuentoAplicado;
    private String codigoUsado;
    private List<DetalleCompraResponseDTO> detalles;
}
