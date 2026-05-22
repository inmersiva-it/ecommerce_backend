package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.ImagenProductoDTO;
import com.ecommerce.ecommerce_backend.service.ImagenProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/productos")
public class ImagenProductoController {

    @Autowired
    private ImagenProductoService imagenProductoService;

    @PostMapping(value = "/{id}/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagenProductoDTO> subirImagen(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {
        ImagenProductoDTO dto = imagenProductoService.subirImagen(id, file);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/imagenes")
    public ResponseEntity<List<ImagenProductoDTO>> listarPorProducto(@PathVariable Integer id) {
        List<ImagenProductoDTO> dtos = imagenProductoService.listarPorProducto(id);
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/imagenes/{id}")
    public ResponseEntity<Void> borrarImagen(@PathVariable Integer id) {
        imagenProductoService.borrarImagen(id);
        return ResponseEntity.noContent().build();
    }
}
