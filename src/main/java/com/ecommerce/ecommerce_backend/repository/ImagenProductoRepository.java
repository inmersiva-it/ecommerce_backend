package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.ImagenProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Integer> {
    List<ImagenProducto> findByProductoId(Integer productoId);
}
