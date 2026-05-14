package com.ecommerce.ecommerce_backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class CompraRequest {
    private List<ItemCompra> items;

    @Data
    public static class ItemCompra {
        private Integer productoId;
        private Integer cantidad;
    }
}
