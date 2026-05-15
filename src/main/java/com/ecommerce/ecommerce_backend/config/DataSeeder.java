package com.ecommerce.ecommerce_backend.config;

import com.ecommerce.ecommerce_backend.entity.Promocion;
import com.ecommerce.ecommerce_backend.repository.PromocionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private PromocionRepository promocionRepository;

    @Override
    public void run(String... args) throws Exception {
        // Insertar cupón CAPIBARA (10% descuento) si no existe
        if (promocionRepository.findByCodigo("CAPIBARA").isEmpty()) {
            Promocion c1 = new Promocion();
            c1.setCodigo("CAPIBARA");
            c1.setPorcentajeDescuento(10);
            c1.setFechaVencimiento(LocalDate.of(2026, 12, 31));
            c1.setActivo(true);
            promocionRepository.save(c1);
        }

        // Insertar cupón PROFE-20 (20% descuento) si no existe
        if (promocionRepository.findByCodigo("PROFE-20").isEmpty()) {
            Promocion c2 = new Promocion();
            c2.setCodigo("PROFE-20");
            c2.setPorcentajeDescuento(20);
            c2.setFechaVencimiento(LocalDate.of(2026, 12, 31));
            c2.setActivo(true);
            promocionRepository.save(c2);
        }
    }
}
