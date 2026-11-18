package com.trotamundos.asistente.servicio;

/** La conversación o la acción no existe, o no pertenece al usuario (404). */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
