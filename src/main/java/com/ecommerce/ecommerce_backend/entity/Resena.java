package com.ecommerce.ecommerce_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "resenas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resena {
    @Id
    private String id;

    private Integer productoId;
    private Integer usuarioId;
    private String usuarioNombre;
    private String usuarioEmail;
    
    private Integer calificacion; // null/0 para las respuestas
    private String comentario;
    private LocalDateTime fechaResena = LocalDateTime.now();
    
    private String parentId; // ID de la reseña/comentario al que responde (null si es nivel superior)
}
