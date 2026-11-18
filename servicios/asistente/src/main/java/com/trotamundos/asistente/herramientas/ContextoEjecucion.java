package com.trotamundos.asistente.herramientas;

import com.trotamundos.asistente.dominio.Conversacion;

/**
 * Datos con los que se ejecuta una herramienta. El usuario sale del token (en el prototipo,
 * del header X-Usuario-Id), nunca de lo que propone el modelo (ADR 019, barrera 7).
 */
public record ContextoEjecucion(String usuarioId, Conversacion conversacion) {
}
