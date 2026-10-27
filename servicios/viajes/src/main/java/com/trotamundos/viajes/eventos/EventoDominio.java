package com.trotamundos.viajes.eventos;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Sobre común de todos los eventos (Iteración 2, sección 5.6; specs/eventos-asyncapi.yaml).
 * Cada servicio define su propia copia: no se comparten clases entre servicios (decisión 4.1).
 */
public record EventoDominio(
        String eventoId,
        String tipo,
        Instant ocurridoEn,
        String usuarioId,
        Map<String, Object> datos) {

    public static EventoDominio nuevo(String tipo, String usuarioId, Map<String, Object> datos) {
        return new EventoDominio(UUID.randomUUID().toString(), tipo, Instant.now(), usuarioId, datos);
    }
}
