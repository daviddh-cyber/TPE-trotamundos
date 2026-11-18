package com.trotamundos.asistente.integracion;

import java.time.OffsetDateTime;
import java.util.Map;

/** Reserva tal como la devuelve el servicio Viajes (specs/viajes-api.yaml). */
public record ReservaResumen(
        String tipo,
        String proveedor,
        String codigo,
        OffsetDateTime fechaInicio,
        OffsetDateTime fechaFin,
        String origen,
        String destino,
        String estado,
        Map<String, Object> detalles) {
}
