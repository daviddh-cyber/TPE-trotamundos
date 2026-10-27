package com.trotamundos.proveedores.api;

import com.trotamundos.proveedores.servicio.ClaveInvalidaException;
import com.trotamundos.proveedores.servicio.ProveedorDesconocidoException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones a las respuestas definidas en la spec (400 y 401). */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(ClaveInvalidaException.class)
    public ResponseEntity<Map<String, String>> claveInvalida(ClaveInvalidaException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler({ProveedorDesconocidoException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> solicitudInvalida(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}
