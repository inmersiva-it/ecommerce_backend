package com.ecommerce.ecommerce_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.ecommerce_backend.entity.Rol;
import com.ecommerce.ecommerce_backend.entity.Usuario;
import com.ecommerce.ecommerce_backend.repository.RolRepository;
import com.ecommerce.ecommerce_backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario login(String email, String password) {

        Optional<Usuario> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return null;
        }

        Usuario user = userOpt.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }

        return user;
    }

    public Usuario register(String nombres, String apellidos, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }
        Usuario user = new Usuario();
        user.setNombre(nombres + " " + apellidos);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        
        // Asignar rol por defecto "Cliente"
        Rol rolCliente = rolRepository.findByNombre("Cliente")
                .orElseThrow(() -> new RuntimeException("Error: Rol 'Cliente' no encontrado."));
        user.setRol(rolCliente);
        
        return userRepository.save(user);
    }

    public Optional<Usuario> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Usuario updateProfile(String email, String nombre) {
        Optional<Usuario> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            Usuario user = userOpt.get();
            user.setNombre(nombre);
            return userRepository.save(user);
        }
        throw new RuntimeException("Usuario no encontrado");
    }
}