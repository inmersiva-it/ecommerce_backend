package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {
    Optional<MetodoPago> findByNombre(String nombre);
}

