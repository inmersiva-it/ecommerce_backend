package com.ecommerce.ecommerce_backend.config;

import com.ecommerce.ecommerce_backend.entity.Promocion;
import com.ecommerce.ecommerce_backend.entity.Rol;
import com.ecommerce.ecommerce_backend.entity.Usuario;
import com.ecommerce.ecommerce_backend.entity.MetodoPago;
import com.ecommerce.ecommerce_backend.repository.PromocionRepository;
import com.ecommerce.ecommerce_backend.repository.RolRepository;
import com.ecommerce.ecommerce_backend.repository.UsuarioRepository;
import com.ecommerce.ecommerce_backend.repository.ProductoRepository;
import com.ecommerce.ecommerce_backend.repository.MetodoPagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private PromocionRepository promocionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {

        // ─── Crear roles base si no existen ─────────────────────────────────
        Rol adminRol = getOrCreateRol("Administrador");
        getOrCreateRol("Cliente");

        // ─── Crear usuarios administradores por defecto si no existen ───────
        getOrCreateAdminUser("admin@ecommerce.com", adminRol);
        getOrCreateAdminUser("partner.despliegue@gmail.com", adminRol);

        // ─── Arreglar usuarios con activo = NULL ────────────────────────────
        List<Usuario> usuarios = usuarioRepository.findAll();
        int corregidos = 0;
        for (Usuario u : usuarios) {
            if (u.getActivo() == null) {
                u.setActivo(true);
                usuarioRepository.save(u);
                corregidos++;
            }
        }
        if (corregidos > 0) {
            System.out.println("[DataSeeder] " + corregidos + " usuario(s) con activo=NULL fueron activados.");
        }

        // ─── Cupones ─────────────────────────────────────────────────────────
        upsertCupon("CAPIBARA", 10, LocalDate.of(2026, 12, 31));
        upsertCupon("PROFE-20", 20, LocalDate.of(2026, 12, 31));

        // ─── Métodos de Pago ───────────────────────────────────────────────
        try {
            jdbcTemplate.update("UPDATE metodos_pago SET nombre = 'Tarjeta de Crédito / Débito' WHERE id = 1");
            jdbcTemplate.update("UPDATE metodos_pago SET nombre = 'Yape' WHERE id = 2");
            jdbcTemplate.update("UPDATE metodos_pago SET nombre = 'Pago en efectivo' WHERE id = 3");
            System.out.println("[DataSeeder] Métodos de pago actualizados en la BD.");
        } catch (Exception e) {
            System.err.println("[DataSeeder] Error al actualizar métodos de pago: " + e.getMessage());
        }

        getOrCreateMetodoPago("Tarjeta de Crédito / Débito");
        getOrCreateMetodoPago("Yape");
        getOrCreateMetodoPago("Pago en efectivo");

        // ─── Limpiar tablas obsoletas ──────────────────────────────────────
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS detalle_compras");
            jdbcTemplate.execute("DROP TABLE IF EXISTS compras");
            System.out.println("[DataSeeder] Tablas obsoletas 'detalle_compras' y 'compras' eliminadas con éxito.");
        } catch (Exception e) {
            System.err.println("[DataSeeder] Error al eliminar tablas obsoletas: " + e.getMessage());
        }
    }

    private void getOrCreateMetodoPago(String nombre) {
        metodoPagoRepository.findByNombre(nombre).orElseGet(() -> {
            MetodoPago mp = new MetodoPago();
            mp.setNombre(nombre);
            System.out.println("[DataSeeder] Método de Pago creado: " + nombre);
            return metodoPagoRepository.save(mp);
        });
    }

    private Rol getOrCreateRol(String nombre) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre(nombre);
            System.out.println("[DataSeeder] Rol creado: " + nombre);
            return rolRepository.save(r);
        });
    }

    private void getOrCreateAdminUser(String email, Rol adminRol) {
        java.util.Optional<Usuario> uOpt = usuarioRepository.findByEmail(email);
        if (uOpt.isEmpty()) {
            Usuario u = new Usuario();
            u.setNombre("Administrador");
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode("Admin1234567*"));
            u.setRol(adminRol);
            u.setActivo(true);
            usuarioRepository.save(u);
            System.out.println("[DataSeeder] Usuario Administrador autocreado: " + email);
        } else {
            Usuario u = uOpt.get();
            boolean modificado = false;
            if (!passwordEncoder.matches("Admin1234567*", u.getPassword())) {
                u.setPassword(passwordEncoder.encode("Admin1234567*"));
                modificado = true;
            }
            if (u.getRol() == null || !u.getRol().getId().equals(adminRol.getId())) {
                u.setRol(adminRol);
                modificado = true;
            }
            if (u.getActivo() == null || !u.getActivo()) {
                u.setActivo(true);
                modificado = true;
            }
            if (modificado) {
                usuarioRepository.save(u);
                System.out.println("[DataSeeder] Credenciales/Rol del usuario administrador actualizadas para: " + email);
            }
        }
    }

    private void upsertCupon(String codigo, int porcentaje, LocalDate vencimiento) {
        java.util.Optional<Promocion> existente = promocionRepository.findByCodigo(codigo);

        if (existente.isPresent()) {
            Promocion p = existente.get();
            if (p.getFechaVencimiento() == null) {
                p.setFechaVencimiento(vencimiento);
                p.setPorcentajeDescuento(porcentaje);
                p.setActivo(true);
                promocionRepository.save(p);
                System.out.println("[DataSeeder] Cupón actualizado: " + codigo);
            }
        } else {
            Promocion p = new Promocion();
            p.setCodigo(codigo);
            p.setPorcentajeDescuento(porcentaje);
            p.setFechaVencimiento(vencimiento);
            p.setActivo(true);
            promocionRepository.save(p);
            System.out.println("[DataSeeder] Cupón creado: " + codigo);
        }
    }

    private void executeSqlFile(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String content = reader.lines()
                        .filter(line -> !line.trim().startsWith("--"))
                        .collect(Collectors.joining("\n"));
                
                String[] statements = content.split(";");
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        jdbcTemplate.execute(trimmed);
                    }
                }
            }
            System.out.println("[DataSeeder] Script ejecutado con éxito: " + resourcePath);
        } catch (Exception e) {
            System.err.println("[DataSeeder] Error al ejecutar script: " + resourcePath + ". Detalle: " + e.getMessage());
            throw new RuntimeException("Error en semillero SQL", e);
        }
    }
}
