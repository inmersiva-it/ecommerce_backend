package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.PromocionDTO;
import com.ecommerce.ecommerce_backend.entity.Promocion;
import com.ecommerce.ecommerce_backend.repository.PromocionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PromocionService {

    @Autowired
    private PromocionRepository promocionRepository;

    public List<PromocionDTO> obtenerTodas() {
        return promocionRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PromocionDTO crear(PromocionDTO dto) {
        if (promocionRepository.findByCodigo(dto.getCodigo()).isPresent()) {
            throw new RuntimeException("Ya existe un cupón con el código: " + dto.getCodigo());
        }
        Promocion p = new Promocion();
        p.setCodigo(dto.getCodigo().toUpperCase().trim());
        p.setPorcentajeDescuento(dto.getPorcentajeDescuento());
        p.setFechaVencimiento(dto.getFechaVencimiento());
        p.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        return toDTO(promocionRepository.save(p));
    }

    public void eliminar(Integer id) {
        if (!promocionRepository.existsById(id)) {
            throw new RuntimeException("Cupón no encontrado: " + id);
        }
        promocionRepository.deleteById(id);
    }

    public PromocionDTO toggleEstado(Integer id) {
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cupón no encontrado: " + id));
        p.setActivo(!p.getActivo());
        return toDTO(promocionRepository.save(p));
    }

    /**
     * Valida un código de cupón: existe, está activo y no ha vencido.
     * Retorna el porcentaje de descuento o lanza RuntimeException.
     */
    public int validarCodigo(String codigo) {
        Promocion p = promocionRepository.findByCodigo(codigo.toUpperCase().trim())
                .orElseThrow(() -> new RuntimeException("Cupón inválido: " + codigo));
        if (!Boolean.TRUE.equals(p.getActivo())) {
            throw new RuntimeException("El cupón está inactivo.");
        }
        if (p.getFechaVencimiento() != null && p.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new RuntimeException("El cupón ha vencido.");
        }
        return p.getPorcentajeDescuento();
    }

    private PromocionDTO toDTO(Promocion p) {
        PromocionDTO dto = new PromocionDTO();
        dto.setId(p.getId());
        dto.setCodigo(p.getCodigo());
        dto.setPorcentajeDescuento(p.getPorcentajeDescuento());
        dto.setFechaVencimiento(p.getFechaVencimiento());
        dto.setActivo(p.getActivo());
        return dto;
    }
}
