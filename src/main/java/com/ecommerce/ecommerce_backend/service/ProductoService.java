package com.ecommerce.ecommerce_backend.service;


import com.ecommerce.ecommerce_backend.entity.ImagenProducto;
import com.ecommerce.ecommerce_backend.entity.Producto;
import com.ecommerce.ecommerce_backend.entity.Resena;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.repository.ImagenProductoRepository;
import com.ecommerce.ecommerce_backend.repository.ProductoRepository;
import com.ecommerce.ecommerce_backend.repository.ResenaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private ImagenProductoRepository imagenProductoRepository;

    @Transactional(readOnly = true)
    public List<Producto> obtenerTodos() {
        List<Producto> productos = productoRepository.findAll();
        for (Producto p : productos) {
            cargarDatosAdicionales(p);
        }
        return productos;
    }

    @Transactional(readOnly = true)
    public List<Producto> obtenerPorCategoria(Integer categoriaId) {
        List<Producto> productos = productoRepository.findByCategoriaId(categoriaId);
        for (Producto p : productos) {
            cargarDatosAdicionales(p);
        }
        return productos;
    }

    @Transactional(readOnly = true)
    public Producto obtenerPorId(Integer id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        cargarDatosAdicionales(p);
        return p;
    }

    private void cargarDatosAdicionales(Producto p) {
        List<Resena> resenas = resenaRepository.findByProductoId(p.getId());
        double sum = 0.0;
        int count = 0;
        for (Resena r : resenas) {
            if ((r.getParentId() == null || r.getParentId().isEmpty()) && r.getCalificacion() != null && r.getCalificacion() > 0) {
                sum += r.getCalificacion();
                count++;
            }
        }
        p.setPromedioCalificaciones(count > 0 ? (sum / count) : 0.0);

        List<String> urls = imagenProductoRepository.findByProductoId(p.getId())
                .stream()
                .map(ImagenProducto::getUrl)
                .collect(Collectors.toList());

        // Include primary image if present and not already listed
        if (p.getImagenUrl() != null && !p.getImagenUrl().isBlank()) {
            if (!urls.contains(p.getImagenUrl())) {
                urls.add(0, p.getImagenUrl());
            }
        }
        p.setImagenes(urls);
    }

}
