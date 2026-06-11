package com.ecommerce.ecommerce_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "reacciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reaccion {
    @Id
    private String id;

    private Integer usuarioId;
    private String usuarioNombre;
    private String usuarioEmail;

    private String tipo; // LIKE, LOVE, HAHA, WOW, SAD, ANGRY
    private String resenaId; // Referencia a la reseña/comentario (puede ser null)
    private String publicacionId; // Referencia al post/publicación de red social (puede ser null)
    private LocalDateTime fechaReaccion = LocalDateTime.now();
}
