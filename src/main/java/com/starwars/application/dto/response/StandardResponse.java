package com.starwars.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardResponse<T> {
    private boolean exito;
    private T datos;
    private String error;
    
    // Métodos helper para crear respuestas exitosas
    public static <T> StandardResponse<T> exito(T datos) {
        return StandardResponse.<T>builder()
                .exito(true)
                .datos(datos)
                .error(null)
                .build();
    }
    
    public static <T> StandardResponse<T> exito(T datos, String mensaje) {
        return StandardResponse.<T>builder()
                .exito(true)
                .datos(datos)
                .error(null)
                .build();
    }
    
    // Métodos helper para crear respuestas de error
    public static <T> StandardResponse<T> error(String mensajeError) {
        return StandardResponse.<T>builder()
                .exito(false)
                .datos(null)
                .error(mensajeError)
                .build();
    }
    
    public static <T> StandardResponse<T> error(String mensaje, String error) {
        return StandardResponse.<T>builder()
                .exito(false)
                .datos(null)
                .error(error)
                .build();
    }
}

