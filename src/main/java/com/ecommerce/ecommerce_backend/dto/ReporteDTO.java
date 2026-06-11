package com.ecommerce.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDTO {
    private List<VentasMes> ventasPorMes;
    private List<ProductoVendido> topProductos;
    private List<EstadoPedido> estadoPedidos;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VentasMes {
        private String mes;
        private BigDecimal ventas;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductoVendido {
        private String nombre;
        private Long cantidad;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EstadoPedido {
        private String estado;
        private Long cantidad;
    }
}
