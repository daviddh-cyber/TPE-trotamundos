package com.trotamundos.asistente.dominio;

import java.time.Instant;

/** Un mensaje de la conversación. */
public record Mensaje(Autor autor, String texto, Instant enviadoEn) {
}
