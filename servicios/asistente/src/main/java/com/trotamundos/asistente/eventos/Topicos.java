package com.trotamundos.asistente.eventos;

/** Tópicos de Kafka. Convención: servicio-que-publica.evento. */
public final class Topicos {

    /** Publica Asistente: CasoDerivado. Consumen Comunicaciones y Analítica (fuera del prototipo). */
    public static final String CASOS_DERIVADOS = "asistente.casos-derivados";

    private Topicos() {
    }
}
