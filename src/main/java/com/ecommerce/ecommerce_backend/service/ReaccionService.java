package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.ReaccionDTO;
import com.ecommerce.ecommerce_backend.dto.ReaccionSaveDTO;
import com.ecommerce.ecommerce_backend.entity.Reaccion;
import com.ecommerce.ecommerce_backend.entity.Usuario;
import com.ecommerce.ecommerce_backend.exception.BadRequestException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.repository.ReaccionRepository;
import com.ecommerce.ecommerce_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ReaccionService {

    @Autowired
    private ReaccionRepository reaccionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private KernelFileLogger kernelFileLogger;

    /*
     * =========================================================================
     *           FUNDAMENTOS DE SISTEMAS OPERATIVOS - EXAMEN PARCIAL 3
     * =========================================================================
     * CONCEPTO: SINCRONIZACIÓN Y EXCLUSIÓN MUTUA (MUTEX)
     * reaccionLock (ReentrantLock) garantiza exclusión mutua para las peticiones
     * concurrentes que manipulan las reacciones en MongoDB. Esto evita que
     * múltiples operaciones del mismo usuario en paralelo sobre el mismo post
     * generen inconsistencias en los registros de reacción.
     * =========================================================================
     */
    private final ReentrantLock reaccionLock = new ReentrantLock();

    public ReaccionDTO reaccionar(String userEmail, ReaccionSaveDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));

        if (dto.getResenaId() == null && dto.getPublicacionId() == null) {
            throw new BadRequestException("Debe especificar una reseña o una publicación para reaccionar");
        }

        reaccionLock.lock();
        try {
            Reaccion reaccion;
            if (dto.getResenaId() != null) {
                Optional<Reaccion> existente = reaccionRepository.findByUsuarioIdAndResenaId(usuario.getId(), dto.getResenaId());
                if (existente.isPresent()) {
                    reaccion = existente.get();
                    if (dto.getTipo() == null || dto.getTipo().trim().isEmpty()) {
                        reaccionRepository.delete(reaccion);
                        kernelFileLogger.logAsync("REACCIONES", "Reacción eliminada por " + userEmail + " en reseña: " + dto.getResenaId());
                        return null;
                    }
                    reaccion.setTipo(dto.getTipo().toUpperCase());
                } else {
                    if (dto.getTipo() == null || dto.getTipo().trim().isEmpty()) {
                        return null;
                    }
                    reaccion = new Reaccion();
                    reaccion.setUsuarioId(usuario.getId());
                    reaccion.setUsuarioNombre(usuario.getNombre());
                    reaccion.setUsuarioEmail(usuario.getEmail());
                    reaccion.setResenaId(dto.getResenaId());
                    reaccion.setTipo(dto.getTipo().toUpperCase());
                }
            } else {
                Optional<Reaccion> existente = reaccionRepository.findByUsuarioIdAndPublicacionId(usuario.getId(), dto.getPublicacionId());
                if (existente.isPresent()) {
                    reaccion = existente.get();
                    if (dto.getTipo() == null || dto.getTipo().trim().isEmpty()) {
                        reaccionRepository.delete(reaccion);
                        kernelFileLogger.logAsync("REACCIONES", "Reacción eliminada por " + userEmail + " en publicación: " + dto.getPublicacionId());
                        return null;
                    }
                    reaccion.setTipo(dto.getTipo().toUpperCase());
                } else {
                    if (dto.getTipo() == null || dto.getTipo().trim().isEmpty()) {
                        return null;
                    }
                    reaccion = new Reaccion();
                    reaccion.setUsuarioId(usuario.getId());
                    reaccion.setUsuarioNombre(usuario.getNombre());
                    reaccion.setUsuarioEmail(usuario.getEmail());
                    reaccion.setPublicacionId(dto.getPublicacionId());
                    reaccion.setTipo(dto.getTipo().toUpperCase());
                }
            }

            reaccion.setFechaReaccion(LocalDateTime.now());
            reaccion = reaccionRepository.save(reaccion);
            kernelFileLogger.logAsync("REACCIONES", "Reacción [" + reaccion.getTipo() + "] registrada por " + userEmail + " en " + 
                    (dto.getResenaId() != null ? "reseña " + dto.getResenaId() : "publicación " + dto.getPublicacionId()));
            return mapToDTO(reaccion);
        } finally {
            reaccionLock.unlock();
        }
    }

    public Map<String, Long> obtenerConteosPorResena(String resenaId) {
        Map<String, Long> conteos = new HashMap<>();
        for (String tipo : List.of("LIKE", "LOVE", "HAHA", "WOW", "SAD", "ANGRY")) {
            conteos.put(tipo, reaccionRepository.countByResenaIdAndTipo(resenaId, tipo));
        }
        return conteos;
    }

    public Map<String, Long> obtenerConteosPorPublicacion(String publicacionId) {
        Map<String, Long> conteos = new HashMap<>();
        for (String tipo : List.of("LIKE", "LOVE", "HAHA", "WOW", "SAD", "ANGRY")) {
            conteos.put(tipo, reaccionRepository.countByPublicacionIdAndTipo(publicacionId, tipo));
        }
        return conteos;
    }

    public String obtenerReaccionDeUsuarioEnResena(Integer usuarioId, String resenaId) {
        return reaccionRepository.findByUsuarioIdAndResenaId(usuarioId, resenaId)
                .map(Reaccion::getTipo)
                .orElse(null);
    }

    public String obtenerReaccionDeUsuarioEnPublicacion(Integer usuarioId, String publicacionId) {
        return reaccionRepository.findByUsuarioIdAndPublicacionId(usuarioId, publicacionId)
                .map(Reaccion::getTipo)
                .orElse(null);
    }

    private ReaccionDTO mapToDTO(Reaccion r) {
        return new ReaccionDTO(
                r.getId(),
                r.getUsuarioNombre(),
                r.getUsuarioEmail(),
                r.getTipo(),
                r.getResenaId(),
                r.getPublicacionId(),
                r.getFechaReaccion()
        );
    }
}
