package com.ecommerce.ecommerce_backend.dto;

import lombok.Data;

@Data
public class UsuarioDTO {
    private Integer id;
    private String nombre;
    private String email;
    private String rol;
    private Boolean activo;
}
