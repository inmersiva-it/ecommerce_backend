package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByUsuarioIdOrderByFechaPedidoDesc(Integer usuarioId);
    List<Pedido> findAllByOrderByFechaPedidoDesc();

    void deleteByUsuarioId(Integer usuarioId);

    @Query(value = "SELECT DATE_FORMAT(p.fecha_pedido, '%Y-%m') AS mes, SUM(p.total) AS ventas " +
                   "FROM pedidos p " +
                   "GROUP BY DATE_FORMAT(p.fecha_pedido, '%Y-%m') " +
                   "ORDER BY mes ASC", nativeQuery = true)
    List<Object[]> getSalesByMonth();

    @Query(value = "SELECT prod.nombre AS nombre, SUM(dp.cantidad) AS vendidos " +
                   "FROM detalle_pedidos dp " +
                   "JOIN productos prod ON dp.producto_id = prod.id " +
                   "GROUP BY prod.id, prod.nombre " +
                   "ORDER BY vendidos DESC " +
                   "LIMIT 5", nativeQuery = true)
    List<Object[]> getTopSellingProducts();

    @Query(value = "SELECT p.estado AS estado, COUNT(p.id) AS cantidad " +
                   "FROM pedidos p " +
                   "GROUP BY p.estado", nativeQuery = true)
    List<Object[]> getOrderStatuses();
}
