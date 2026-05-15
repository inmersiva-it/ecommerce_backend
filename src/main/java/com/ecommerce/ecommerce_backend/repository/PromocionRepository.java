package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Integer> {
    Optional<Promocion> findByCodigo(String codigo);
}
