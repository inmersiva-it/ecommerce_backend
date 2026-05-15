package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.UsuarioDTO;
import com.ecommerce.ecommerce_backend.entity.Rol;
import com.ecommerce.ecommerce_backend.entity.Usuario;
import com.ecommerce.ecommerce_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<UsuarioDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UsuarioDTO toggleEstado(Integer id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        u.setActivo(!u.getActivo());
        usuarioRepository.save(u);
        return toDTO(u);
    }

    private UsuarioDTO toDTO(Usuario u) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(u.getId());
        dto.setNombre(u.getNombre());
        dto.setEmail(u.getEmail());
        dto.setActivo(u.getActivo());
        if (u.getRol() != null) {
            dto.setRol(u.getRol().getNombre());
        } else {
            dto.setRol("SIN ROL");
        }
        return dto;
    }
}
