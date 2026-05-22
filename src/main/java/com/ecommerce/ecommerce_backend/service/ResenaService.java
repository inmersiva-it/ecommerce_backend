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
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public ResenaDTO crearResena(String userEmail, Integer productoId, ResenaSaveDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        if (dto.getCalificacion() == null || dto.getCalificacion() < 1 || dto.getCalificacion() > 5) {
            throw new BadRequestException("La calificación debe estar entre 1 y 5 estrellas");
        }

        Resena resena = new Resena();
        resena.setProducto(producto);
        resena.setUsuario(usuario);
        resena.setCalificacion(dto.getCalificacion());
        resena.setComentario(dto.getComentario());
        resena.setFechaResena(LocalDateTime.now());

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
        Double avg = resenaRepository.findAverageCalificacionByProductoId(productoId);
        return avg != null ? avg : 0.0;
    }

    private ResenaDTO mapToDTO(Resena r) {
        return new ResenaDTO(
                r.getId(),
                r.getCalificacion(),
                r.getComentario(),
                r.getFechaResena(),
                r.getUsuario().getNombre(),
                r.getUsuario().getEmail(),
                r.getProducto().getId()
        );
    }
}
