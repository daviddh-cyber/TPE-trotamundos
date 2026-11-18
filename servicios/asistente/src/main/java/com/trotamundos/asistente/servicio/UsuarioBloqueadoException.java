package com.trotamundos.asistente.servicio;

/** El usuario no puede usar el asistente por intentos de manipulación (403; ADR 019, barrera 9). */
public class UsuarioBloqueadoException extends RuntimeException {

    public UsuarioBloqueadoException(String mensaje) {
        super(mensaje);
    }
}
