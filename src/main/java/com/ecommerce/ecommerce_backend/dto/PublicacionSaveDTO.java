package com.ecommerce.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicacionSaveDTO {
    private String titulo;
    private String descripcion;
    private List<Integer> productoIds;
}
