package com.trotamundos.proveedores.eventos;

/** Tópicos de Kafka. Convención: servicio-que-publica.evento. */
public final class Topicos {

    /** Publica Viajes: ReservaRegistrada. Este servicio lo consume para empezar a monitorear la reserva. */
    public static final String RESERVAS_REGISTRADAS = "viajes.reservas-registradas";

    /** Publica este servicio: ReservaActualizada y ReservaCancelada. */
    public static final String CAMBIOS_RESERVA = "proveedores.cambios-reserva";

    private Topicos() {
    }
}
