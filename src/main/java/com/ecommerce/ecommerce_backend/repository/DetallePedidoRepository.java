package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {
}
