package com.ecommerce.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReaccionDTO {
    private String id;
    private String usuarioNombre;
    private String usuarioEmail;
    private String tipo;
    private String resenaId;
    private String publicacionId;
    private LocalDateTime fechaReaccion;
}
