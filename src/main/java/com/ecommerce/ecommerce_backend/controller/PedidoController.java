package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.PedidoDTO;
import com.ecommerce.ecommerce_backend.dto.PedidoSaveDTO;
import com.ecommerce.ecommerce_backend.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoDTO> crearPedido(@RequestBody PedidoSaveDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        PedidoDTO creado = pedidoService.crearPedido(email, dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<PedidoDTO>> obtenerMisPedidos() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<PedidoDTO> pedidos = pedidoService.obtenerPedidosPorUsuario(email);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping
    public ResponseEntity<List<PedidoDTO>> obtenerTodos() {
        List<PedidoDTO> pedidos = pedidoService.obtenerTodosLosPedidos();
        return ResponseEntity.ok(pedidos);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<PedidoDTO> actualizarEstado(
            @PathVariable Integer id,
            @RequestParam("estado") String estado) {
        PedidoDTO actualizado = pedidoService.actualizarEstado(id, estado);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPedido(@PathVariable Integer id) {
        pedidoService.eliminarPedido(id);
        return ResponseEntity.noContent().build();
    }
}
