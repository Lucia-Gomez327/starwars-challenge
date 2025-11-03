package com.starwars.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "El nombre de usuario es requerido")
    private String username;
    
    @NotBlank(message = "La contraseña es requerida")
    private String password;
}




