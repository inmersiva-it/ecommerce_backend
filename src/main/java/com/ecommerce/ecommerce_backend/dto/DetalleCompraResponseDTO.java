package com.ecommerce.ecommerce_backend.dto;

import lombok.Data;

@Data
public class DetalleCompraResponseDTO {
    private Long id;
    private String productoNombre;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
}
