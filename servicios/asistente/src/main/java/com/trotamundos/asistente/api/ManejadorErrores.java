package com.trotamundos.asistente.api;

import com.trotamundos.asistente.servicio.AccionNoDisponibleException;
import com.trotamundos.asistente.servicio.RecursoNoEncontradoException;
import com.trotamundos.asistente.servicio.UsuarioBloqueadoException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones a las respuestas definidas en la spec (400, 403, 404 y 409). */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(RecursoNoEncontradoException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(AccionNoDisponibleException.class)
    public ResponseEntity<Map<String, String>> accionNoDisponible(AccionNoDisponibleException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(UsuarioBloqueadoException.class)
    public ResponseEntity<Map<String, String>> usuarioBloqueado(UsuarioBloqueadoException e) {
        return error(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("error", mensaje));
    }
}
