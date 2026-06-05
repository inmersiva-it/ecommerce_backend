package com.ecommerce.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResenaDTO {
    private String id;
    private Integer calificacion;
    private String comentario;
    private LocalDateTime fechaResena;
    private String usuarioNombre;
    private String usuarioEmail;
    private Integer productoId;
    private String parentId;
}
