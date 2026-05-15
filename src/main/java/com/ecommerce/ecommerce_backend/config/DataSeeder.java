package com.ecommerce.ecommerce_backend.config;

import com.ecommerce.ecommerce_backend.entity.Promocion;
import com.ecommerce.ecommerce_backend.entity.Usuario;
import com.ecommerce.ecommerce_backend.repository.PromocionRepository;
import com.ecommerce.ecommerce_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private PromocionRepository promocionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) throws Exception {
        // ─── Arreglar usuarios con activo = NULL ────────────────────────────
        // Todos los usuarios existentes que tengan NULL deben ser activados
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
}
