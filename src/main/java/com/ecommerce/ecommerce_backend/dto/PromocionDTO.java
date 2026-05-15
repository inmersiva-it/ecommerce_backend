package com.ecommerce.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromocionDTO {
    private Integer id;
    private String codigo;
    private Integer porcentajeDescuento;
    private LocalDate fechaVencimiento;
    private Boolean activo;
}
