package com.ecommerce.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraRequest {
    private List<ItemCompra> items;
    private String codigoPromocion; // opcional

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemCompra {
        private Integer productoId;
        private Integer cantidad;
    }
}
