package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.MetodoPagoDTO;
import com.ecommerce.ecommerce_backend.repository.MetodoPagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/metodos-pago")
public class MetodoPagoController {

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @GetMapping
    public List<MetodoPagoDTO> listarTodos() {
        return metodoPagoRepository.findAll().stream()
                .map(m -> new MetodoPagoDTO(m.getId(), m.getNombre()))
                .collect(Collectors.toList());
    }
}
