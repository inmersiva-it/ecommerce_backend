package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByUsuarioIdOrderByFechaPedidoDesc(Integer usuarioId);
    List<Pedido> findAllByOrderByFechaPedidoDesc();

    void deleteByUsuarioId(Integer usuarioId);
}
