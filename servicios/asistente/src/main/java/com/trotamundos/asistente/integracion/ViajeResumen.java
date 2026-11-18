package com.trotamundos.asistente.integracion;

import java.time.LocalDate;
import java.util.List;

/** Viaje tal como lo devuelve GET /v1/viajes del servicio Viajes (specs/viajes-api.yaml). */
public record ViajeResumen(
        String nombre,
        String destino,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        List<ReservaResumen> reservas) {
}
