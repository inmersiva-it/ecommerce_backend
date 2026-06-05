package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.Resena;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResenaRepository extends MongoRepository<Resena, String> {
    List<Resena> findByProductoIdOrderByFechaResenaDesc(Integer productoId);
    List<Resena> findByProductoId(Integer productoId);
    void deleteByUsuarioId(Integer usuarioId);
    void deleteByProductoId(Integer productoId);
}
