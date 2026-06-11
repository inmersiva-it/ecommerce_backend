package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.PublicacionDTO;
import com.ecommerce.ecommerce_backend.service.PublicacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/publicaciones")
public class PublicacionController {

    @Autowired
    private PublicacionService publicacionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PublicacionDTO> crearPublicacion(
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam(value = "productoIds", required = false) List<Integer> productoIds,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        PublicacionDTO pub = publicacionService.crearPublicacion(email, titulo, descripcion, productoIds, file);
        return new ResponseEntity<>(pub, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PublicacionDTO>> listarTodas() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<PublicacionDTO> publicaciones = publicacionService.obtenerTodas(email);
        return ResponseEntity.ok(publicaciones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicacionDTO> obtenerPorId(@PathVariable String id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        PublicacionDTO pub = publicacionService.obtenerPorId(id, email);
        return ResponseEntity.ok(pub);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPublicacion(@PathVariable String id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        publicacionService.eliminarPublicacion(id, email);
        return ResponseEntity.noContent().build();
    }
}
