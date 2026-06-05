package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.ResenaDTO;
import com.ecommerce.ecommerce_backend.dto.ResenaSaveDTO;
import com.ecommerce.ecommerce_backend.entity.Producto;
import com.ecommerce.ecommerce_backend.entity.Resena;
import com.ecommerce.ecommerce_backend.entity.Usuario;
import com.ecommerce.ecommerce_backend.exception.BadRequestException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.repository.ProductoRepository;
import com.ecommerce.ecommerce_backend.repository.ResenaRepository;
import com.ecommerce.ecommerce_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public ResenaDTO crearResena(String userEmail, Integer productoId, ResenaSaveDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        Resena resena = new Resena();
        resena.setProductoId(productoId);
        resena.setUsuarioId(usuario.getId());
        resena.setUsuarioNombre(usuario.getNombre());
        resena.setUsuarioEmail(usuario.getEmail());
        resena.setComentario(dto.getComentario());
        resena.setFechaResena(LocalDateTime.now());

        if (dto.getParentId() != null && !dto.getParentId().trim().isEmpty()) {
            // Es una respuesta a un comentario
            Resena parent = resenaRepository.findById(dto.getParentId().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Comentario principal no encontrado con ID: " + dto.getParentId()));
            
            resena.setParentId(parent.getId());
            resena.setCalificacion(null); // Las respuestas no tienen calificación
        } else {
            // Es una reseña principal
            if (dto.getCalificacion() == null || dto.getCalificacion() < 1 || dto.getCalificacion() > 5) {
                throw new BadRequestException("La calificación debe estar entre 1 y 5 estrellas");
            }
            resena.setCalificacion(dto.getCalificacion());
            resena.setParentId(null);
        }

        resena = resenaRepository.save(resena);
        return mapToDTO(resena);
    }

    public List<ResenaDTO> obtenerResenasPorProducto(Integer productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + productoId);
        }
        return resenaRepository.findByProductoIdOrderByFechaResenaDesc(productoId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Double obtenerPromedioCalificaciones(Integer productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + productoId);
        }
        List<Resena> resenas = resenaRepository.findByProductoId(productoId);
        if (resenas.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        int count = 0;
        for (Resena r : resenas) {
            // Solo promedia reseñas de nivel superior (parentId es null o vacío) con estrellas válidas
            if ((r.getParentId() == null || r.getParentId().isEmpty()) && r.getCalificacion() != null && r.getCalificacion() > 0) {
                sum += r.getCalificacion();
                count++;
            }
        }
        return count > 0 ? (sum / count) : 0.0;
    }

    private ResenaDTO mapToDTO(Resena r) {
        return new ResenaDTO(
                r.getId(),
                r.getCalificacion(),
                r.getComentario(),
                r.getFechaResena(),
                r.getUsuarioNombre(),
                r.getUsuarioEmail(),
                r.getProductoId(),
                r.getParentId()
        );
    }
}
