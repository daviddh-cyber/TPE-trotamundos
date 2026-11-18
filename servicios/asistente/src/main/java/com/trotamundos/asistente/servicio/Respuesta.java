package com.trotamundos.asistente.servicio;

import com.trotamundos.asistente.dominio.AccionPendiente;

/** Respuesta del asistente. accionPendiente es null si no hay nada para confirmar. */
public record Respuesta(String texto, OrigenRespuesta origen, AccionPendiente accionPendiente) {

    public static Respuesta de(String texto, OrigenRespuesta origen) {
        return new Respuesta(texto, origen, null);
    }
}
