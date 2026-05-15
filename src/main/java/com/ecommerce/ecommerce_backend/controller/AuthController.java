package com.ecommerce.ecommerce_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.ecommerce_backend.dto.AuthResponse;
import com.ecommerce.ecommerce_backend.dto.LoginRequest;
import com.ecommerce.ecommerce_backend.dto.RegisterRequest;
import com.ecommerce.ecommerce_backend.entity.Usuario;
import com.ecommerce.ecommerce_backend.security.JwtUtil;
import com.ecommerce.ecommerce_backend.service.AuthService;

import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    private AuthResponse mapToResponse(String token, Usuario user) {
        String nombre = user.getNombre() != null ? user.getNombre() : user.getEmail().split("@")[0];
        String rol = user.getRol() != null ? user.getRol().getNombre() : "Cliente";
        return new AuthResponse(token, nombre, user.getEmail(), rol);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Usuario user = authService.login(request.getEmail(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }
        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(mapToResponse(token, user));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            Usuario user = authService.register(
                    request.getNombres(), 
                    request.getApellidos(), 
                    request.getEmail(), 
                    request.getPassword()
            );
            String token = jwtUtil.generateToken(user.getEmail());
            return ResponseEntity.ok(mapToResponse(token, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String email = jwtUtil.extractUsername(token);
            Optional<Usuario> userOpt = authService.findByEmail(email);
            if (userOpt.isPresent()) {
                return ResponseEntity.ok(mapToResponse(null, userOpt.get()));
            }
        }
        return ResponseEntity.status(401).body("No autorizado");
    }

    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String token, @RequestBody RegisterRequest request) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String email = jwtUtil.extractUsername(token);
            try {
                // Assuming updateProfile now takes nombres + apellidos or just concatenates
                String fullNombre = request.getNombres() + " " + request.getApellidos();
                Usuario user = authService.updateProfile(email, fullNombre);
                return ResponseEntity.ok(mapToResponse(null, user));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.status(401).body("No autorizado");
    }

    @GetMapping("/test")
    public String test() {
        return "Ruta protegida funcionando";
    }

    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody java.util.Map<String, String> request) {
        String email = request.get("email");
        Optional<Usuario> userOpt = authService.findByEmail(email);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(404).body("El correo no existe en la base de datos");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody java.util.Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("password");
        try {
            authService.resetPassword(email, newPassword);
            return ResponseEntity.ok("Contraseña actualizada correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}