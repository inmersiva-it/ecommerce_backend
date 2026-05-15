package com.ecommerce.ecommerce_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "detalle_compras")
@Data
public class DetalleCompra {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compra_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Double precioUnitario;

    @Column(nullable = false)
    private Double subtotal;
}
