package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.UsuarioDTO;
import com.ecommerce.ecommerce_backend.entity.Rol;
import com.ecommerce.ecommerce_backend.entity.Usuario;
import com.ecommerce.ecommerce_backend.repository.RolRepository;
import com.ecommerce.ecommerce_backend.repository.UsuarioRepository;
import com.ecommerce.ecommerce_backend.repository.ResenaRepository;
import com.ecommerce.ecommerce_backend.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    public List<UsuarioDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UsuarioDTO toggleEstado(Integer id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));

        // Si se intenta bloquear (activo → inactivo), proteger al último admin activo
        if (Boolean.TRUE.equals(u.getActivo())) {
            boolean esAdmin = u.getRol() != null && "Administrador".equalsIgnoreCase(u.getRol().getNombre());
            if (esAdmin) {
                long adminsActivos = usuarioRepository.countActiveAdmins();
                if (adminsActivos <= 1) {
                    throw new RuntimeException("No se puede bloquear al único administrador activo del sistema.");
                }
            }
        }

        u.setActivo(!u.getActivo());
        usuarioRepository.save(u);
        return toDTO(u);
    }

    public UsuarioDTO cambiarRol(Integer id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));

        // Determinar el nuevo rol (toggle: Administrador ↔ Cliente)
        String rolActual = u.getRol() != null ? u.getRol().getNombre() : "Cliente";
        String nuevoRolNombre = "Administrador".equalsIgnoreCase(rolActual) ? "Cliente" : "Administrador";

        // Si se intenta quitar el rol de admin, proteger al último admin activo
        if ("Administrador".equalsIgnoreCase(rolActual)) {
            long adminsActivos = usuarioRepository.countActiveAdmins();
            if (adminsActivos <= 1 && Boolean.TRUE.equals(u.getActivo())) {
                throw new RuntimeException("No se puede cambiar el rol del único administrador activo del sistema.");
            }
        }

        Rol nuevoRol = rolRepository.findByNombre(nuevoRolNombre)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + nuevoRolNombre));

        u.setRol(nuevoRol);
        usuarioRepository.save(u);
        return toDTO(u);
    }

    @Transactional
    public void eliminarUsuario(Integer id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));

        boolean esAdmin = u.getRol() != null && "Administrador".equalsIgnoreCase(u.getRol().getNombre());
        if (esAdmin && Boolean.TRUE.equals(u.getActivo())) {
            long adminsActivos = usuarioRepository.countActiveAdmins();
            if (adminsActivos <= 1) {
                throw new RuntimeException("No se puede eliminar al único administrador activo del sistema.");
            }
        }

        resenaRepository.deleteByUsuarioId(id);
        pedidoRepository.deleteByUsuarioId(id);

        usuarioRepository.delete(u);
    }

    private UsuarioDTO toDTO(Usuario u) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(u.getId());
        dto.setNombre(u.getNombre());
        dto.setEmail(u.getEmail());
        dto.setActivo(u.getActivo() != null ? u.getActivo() : true);
        if (u.getRol() != null) {
            dto.setRol(u.getRol().getNombre());
        } else {
            dto.setRol("Cliente");
        }
        return dto;
    }
}
