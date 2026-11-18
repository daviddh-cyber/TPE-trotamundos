package com.trotamundos.asistente.servicio;

/** De dónde salió la respuesta (specs/asistente-api.yaml). */
public enum OrigenRespuesta {
    /** Consulta de estado respondida con una plantilla, sin LLM (ADR 017). */
    CAMINO_RAPIDO,
    /** Respuesta armada a partir de la interpretación del modelo de lenguaje. */
    MODELO,
    /** Resultado de una acción confirmada por el usuario. */
    ACCION
}
