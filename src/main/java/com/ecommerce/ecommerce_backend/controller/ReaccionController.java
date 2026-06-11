package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.ReaccionDTO;
import com.ecommerce.ecommerce_backend.dto.ReaccionSaveDTO;
import com.ecommerce.ecommerce_backend.service.ReaccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/reacciones")
public class ReaccionController {

    @Autowired
    private ReaccionService reaccionService;

    @PostMapping
    public ResponseEntity<ReaccionDTO> reaccionar(@RequestBody ReaccionSaveDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ReaccionDTO reaccion = reaccionService.reaccionar(email, dto);
        return ResponseEntity.ok(reaccion);
    }
}
