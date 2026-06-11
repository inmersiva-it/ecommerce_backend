package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.entity.Producto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicacionDTO {
    private String id;
    private String titulo;
    private String descripcion;
    private String imagenUrl;
    private List<Producto> productosEtiquetados;
    private String usuarioNombre;
    private String usuarioEmail;
    private LocalDateTime fechaPublicacion;
    private Map<String, Long> reacciones; // Conteo de tipos: e.g., "LIKE": 5, "LOVE": 3
    private String reaccionUsuarioActual; // Tipo de reacción del usuario actual (si existe)
}
