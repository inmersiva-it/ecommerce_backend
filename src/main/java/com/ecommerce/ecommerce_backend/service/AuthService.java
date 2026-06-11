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
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        Optional<Usuario> userOpt = userRepository.findByEmail(normalizedEmail);

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
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }
        Usuario user = new Usuario();
        user.setNombre(nombres + " " + apellidos);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        
        // Asignar rol por defecto "Cliente"
        Rol rolCliente = rolRepository.findByNombre("Cliente")
                .orElseThrow(() -> new RuntimeException("Error: Rol 'Cliente' no encontrado."));
        user.setRol(rolCliente);
        user.setActivo(true);
        
        return userRepository.save(user);
    }

    public Optional<Usuario> findByEmail(String email) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        return userRepository.findByEmail(normalizedEmail);
    }

    public void resetPassword(String email, String newPassword) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        Usuario user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void generateResetToken(String email) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        Usuario user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("El correo no está registrado en el sistema."));

        // Generar un token aleatorio de 6 dígitos
        String token = String.format("%06d", new java.util.Random().nextInt(1000000));
        user.setResetToken(token);
        user.setTokenExpiry(java.time.LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        /*
         * =========================================================================
         *           FUNDAMENTOS DE SISTEMAS OPERATIVOS - EXAMEN PARCIAL 3
         * =========================================================================
         * CONCEPTO: INTERACCIÓN CON EL KERNEL Y DISPOSITIVOS (SYSTEM CALLS - I/O)
         * Simulamos el envío del correo electrónico escribiendo directamente en el
         * sistema de archivos del sistema operativo mediante la biblioteca nativa
         * Java NIO (Files.write). Esto genera internamente llamadas al sistema como
         * sys_open y sys_write delegadas al Kernel de OS.
         * =========================================================================
         */
        try {
            java.nio.file.Path logPath = java.nio.file.Paths.get("correos_salientes.log");
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String emailContent = String.format(
                "[%s] Para: %s\nAsunto: Recuperación de Contraseña - PC Capibara\nCuerpo: Tu token de recuperación de contraseña es: %s. Expira en 15 minutos.\n------------------------------------------------------------\n",
                timestamp, normalizedEmail, token
            );
            java.nio.file.Files.write(
                logPath, 
                emailContent.getBytes(java.nio.charset.StandardCharsets.UTF_8), 
                java.nio.file.StandardOpenOption.CREATE, 
                java.nio.file.StandardOpenOption.APPEND
            );
        } catch (java.io.IOException e) {
            System.err.println("Error al simular envío de correo en correos_salientes.log: " + e.getMessage());
        }
    }

    public void resetPasswordWithToken(String email, String token, String newPassword) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        Usuario user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (user.getResetToken() == null || !user.getResetToken().equals(token.trim())) {
            throw new RuntimeException("El token de recuperación es incorrecto.");
        }

        if (user.getTokenExpiry() == null || user.getTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("El token de recuperación ha expirado.");
        }

        // Encriptación y guardado
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);
    }

    public Usuario updateProfile(String email, String nombre) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        Optional<Usuario> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isPresent()) {
            Usuario user = userOpt.get();
            user.setNombre(nombre);
            return userRepository.save(user);
        }
        throw new RuntimeException("Usuario no encontrado");
    }
}