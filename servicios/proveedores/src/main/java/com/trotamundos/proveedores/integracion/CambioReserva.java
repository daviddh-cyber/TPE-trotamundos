package com.trotamundos.proveedores.integracion;

import java.util.Map;

/**
 * Cambio de una reserva en el modelo común de Trotamundos.
 * Cada adaptador traduce el formato propio de su proveedor a este registro (ADR 007).
 */
public record CambioReserva(String codigo, Tipo tipo, Map<String, Object> cambios, String motivo) {

    public enum Tipo {
        ACTUALIZACION,
        CANCELACION
    }
}
