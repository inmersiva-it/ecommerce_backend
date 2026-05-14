package com.ecommerce.ecommerce_backend.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String nombres;
    private String apellidos;
    private String email;
    private String password;
}
