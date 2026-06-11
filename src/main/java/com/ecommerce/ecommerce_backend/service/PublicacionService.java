package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.PublicacionDTO;
import com.ecommerce.ecommerce_backend.entity.Producto;
import com.ecommerce.ecommerce_backend.entity.Publicacion;
import com.ecommerce.ecommerce_backend.entity.Usuario;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.repository.ProductoRepository;
import com.ecommerce.ecommerce_backend.repository.PublicacionRepository;
import com.ecommerce.ecommerce_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PublicacionService {

    @Autowired
    private PublicacionRepository publicacionRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReaccionService reaccionService;

    @Autowired
    private KernelFileLogger kernelFileLogger;

    private final Path rootPath = Paths.get("uploads");

    public PublicacionDTO crearPublicacion(String userEmail, String titulo, String descripcion, List<Integer> productoIds, MultipartFile file) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));

        String imagenUrl = null;
        if (file != null && !file.isEmpty()) {
            try {
                // System Call para crear directorios
                if (!Files.exists(rootPath)) {
                    Files.createDirectories(rootPath);
                }

                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String filename = UUID.randomUUID().toString() + extension;
                Path filePath = this.rootPath.resolve(filename);

                // System Call para copiar archivo (I/O de disco)
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                imagenUrl = "/uploads/" + filename;
            } catch (IOException e) {
                throw new RuntimeException("Error al almacenar el archivo de la publicación: " + e.getMessage(), e);
            }
        }

        Publicacion pub = new Publicacion();
        pub.setTitulo(titulo);
        pub.setDescripcion(descripcion);
        pub.setImagenUrl(imagenUrl);
        pub.setUsuarioNombre(usuario.getNombre());
        pub.setUsuarioEmail(usuario.getEmail());
        pub.setFechaPublicacion(LocalDateTime.now());
        
        if (productoIds != null) {
            pub.setProductoIds(productoIds);
        }

        pub = publicacionRepository.save(pub);

        kernelFileLogger.logAsync("SOCIAL", "Publicación #" + pub.getId() + " creada por " + userEmail + " con " + 
                (productoIds != null ? productoIds.size() : 0) + " productos etiquetados.");

        return mapToDTO(pub, usuario.getId());
    }

    public List<PublicacionDTO> obtenerTodas(String currentUserEmail) {
        Integer usuarioId = null;
        if (currentUserEmail != null && !currentUserEmail.trim().isEmpty() && !currentUserEmail.equals("anonymousUser")) {
            Optional<Usuario> u = usuarioRepository.findByEmail(currentUserEmail);
            if (u.isPresent()) {
                usuarioId = u.get().getId();
            }
        }

        final Integer finalUsuarioId = usuarioId;
        return publicacionRepository.findAllByOrderByFechaPublicacionDesc()
                .stream()
                .map(pub -> mapToDTO(pub, finalUsuarioId))
                .collect(Collectors.toList());
    }

    public PublicacionDTO obtenerPorId(String id, String currentUserEmail) {
        Publicacion pub = publicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada con ID: " + id));

        Integer usuarioId = null;
        if (currentUserEmail != null && !currentUserEmail.trim().isEmpty() && !currentUserEmail.equals("anonymousUser")) {
            Optional<Usuario> u = usuarioRepository.findByEmail(currentUserEmail);
            if (u.isPresent()) {
                usuarioId = u.get().getId();
            }
        }

        return mapToDTO(pub, usuarioId);
    }

    public void eliminarPublicacion(String id, String userEmail) {
        Publicacion pub = publicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada con ID: " + id));

        // Permitir eliminar si es el creador o si es admin (por simplicidad, permitimos al creador o al admin)
        // Buscamos al usuario para ver si es ADMIN
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));

        boolean isAdmin = usuario.getRol() != null && usuario.getRol().getNombre().equals("Administrador");
        if (!pub.getUsuarioEmail().equals(userEmail) && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("No tiene permisos para eliminar esta publicación");
        }

        publicacionRepository.delete(pub);
        kernelFileLogger.logAsync("SOCIAL", "Publicación #" + id + " eliminada por " + userEmail);
    }

    private PublicacionDTO mapToDTO(Publicacion pub, Integer currentUsuarioId) {
        // Cargar productos etiquetados de MariaDB
        List<Producto> productos = new ArrayList<>();
        if (pub.getProductoIds() != null && !pub.getProductoIds().isEmpty()) {
            productos = productoRepository.findAllById(pub.getProductoIds());
        }

        // Obtener conteo de reacciones en MongoDB
        var reaccionesCount = reaccionService.obtenerConteosPorPublicacion(pub.getId());

        // Obtener la reacción del usuario actual si está logueado
        String reaccionUsuario = null;
        if (currentUsuarioId != null) {
            reaccionUsuario = reaccionService.obtenerReaccionDeUsuarioEnPublicacion(currentUsuarioId, pub.getId());
        }

        return new PublicacionDTO(
                pub.getId(),
                pub.getTitulo(),
                pub.getDescripcion(),
                pub.getImagenUrl(),
                productos,
                pub.getUsuarioNombre(),
                pub.getUsuarioEmail(),
                pub.getFechaPublicacion(),
                reaccionesCount,
                reaccionUsuario
        );
    }
}
