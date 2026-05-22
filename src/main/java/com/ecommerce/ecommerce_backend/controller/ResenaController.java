package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.ResenaDTO;
import com.ecommerce.ecommerce_backend.dto.ResenaSaveDTO;
import com.ecommerce.ecommerce_backend.service.ResenaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/productos")
public class ResenaController {

    @Autowired
    private ResenaService resenaService;

    @PostMapping("/{id}/resenas")
    public ResponseEntity<ResenaDTO> crearResena(
            @PathVariable Integer id,
            @RequestBody ResenaSaveDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ResenaDTO creadas = resenaService.crearResena(email, id, dto);
        return new ResponseEntity<>(creadas, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/resenas")
    public ResponseEntity<List<ResenaDTO>> listarPorProducto(@PathVariable Integer id) {
        List<ResenaDTO> resenas = resenaService.obtenerResenasPorProducto(id);
        return ResponseEntity.ok(resenas);
    }

    @GetMapping("/{id}/promedio-resenas")
    public ResponseEntity<Double> obtenerPromedio(@PathVariable Integer id) {
        Double promedio = resenaService.obtenerPromedioCalificaciones(id);
        return ResponseEntity.ok(promedio);
    }
}
