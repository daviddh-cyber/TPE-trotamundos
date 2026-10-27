package com.trotamundos.viajes.eventos;

/** Tópicos de Kafka. Convención: servicio-que-publica.evento. */
public final class Topicos {

    /** Publica Viajes: ReservaRegistrada. Consume Proveedores. */
    public static final String RESERVAS_REGISTRADAS = "viajes.reservas-registradas";

    /** Publica Viajes: ViajeActualizado. Consumen Compartir, Comunicaciones y Analítica (fuera del prototipo). */
    public static final String VIAJES_ACTUALIZADOS = "viajes.viajes-actualizados";

    /** Publica Proveedores: ReservaActualizada y ReservaCancelada. Consume Viajes. */
    public static final String CAMBIOS_RESERVA = "proveedores.cambios-reserva";

    private Topicos() {
    }
}
