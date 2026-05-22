package com.ecommerce.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoSaveDTO {
    private Integer metodoPagoId;
    private String codigoPromocion;
    private List<ItemPedidoSave> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemPedidoSave {
        private Integer productoId;
        private Integer cantidad;
    }
}
