package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.PromocionDTO;
import com.ecommerce.ecommerce_backend.service.PromocionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/promociones")
public class PromocionController {

    @Autowired
    private PromocionService promocionService;

    @GetMapping
    public ResponseEntity<List<PromocionDTO>> obtenerTodas() {
        return ResponseEntity.ok(promocionService.obtenerTodas());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody PromocionDTO dto) {
        try {
            return ResponseEntity.ok(promocionService.crear(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            promocionService.eliminar(id);
            return ResponseEntity.ok("Cupón eliminado");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> toggleEstado(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(promocionService.toggleEstado(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Endpoint público para que el carrito valide un cupón antes de confirmar */
    @GetMapping("/validar/{codigo}")
    public ResponseEntity<?> validar(@PathVariable String codigo) {
        try {
            int porcentaje = promocionService.validarCodigo(codigo);
            return ResponseEntity.ok(porcentaje);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
