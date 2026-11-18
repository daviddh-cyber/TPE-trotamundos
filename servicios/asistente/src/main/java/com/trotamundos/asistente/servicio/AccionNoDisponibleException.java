package com.trotamundos.asistente.servicio;

/** La acción ya fue resuelta o venció (409). */
public class AccionNoDisponibleException extends RuntimeException {

    public AccionNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
