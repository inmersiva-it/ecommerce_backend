package com.ecommerce.ecommerce_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "publicaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Publicacion {
    @Id
    private String id;

    private String titulo;
    private String descripcion;
    private String imagenUrl; // Imagen de la publicación
    
    // Lista de IDs de productos de MariaDB etiquetados en esta publicación
    private List<Integer> productoIds = new ArrayList<>();
    
    private String usuarioNombre;
    private String usuarioEmail;
    
    private LocalDateTime fechaPublicacion = LocalDateTime.now();
}
