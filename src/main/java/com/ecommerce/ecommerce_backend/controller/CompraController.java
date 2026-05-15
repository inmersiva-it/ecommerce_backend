package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.CompraRequest;
import com.ecommerce.ecommerce_backend.dto.CompraResponseDTO;
import com.ecommerce.ecommerce_backend.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/compras")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @PostMapping
    public ResponseEntity<?> realizarCompra(@RequestBody CompraRequest request) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            compraService.realizarCompra(email, request.getItems(), request.getCodigoPromocion());
            return ResponseEntity.ok("Compra realizada con éxito");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }

    @GetMapping
    public ResponseEntity<List<CompraResponseDTO>> obtenerTodasLasCompras() {
        return ResponseEntity.ok(compraService.obtenerTodasLasCompras());
    }

    @GetMapping("/mis-compras")
    public ResponseEntity<List<CompraResponseDTO>> obtenerMisCompras() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(compraService.obtenerMisCompras(email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCompra(@PathVariable Long id) {
        try {
            compraService.eliminarCompra(id);
            return ResponseEntity.ok("Compra eliminada con éxito");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }
}
