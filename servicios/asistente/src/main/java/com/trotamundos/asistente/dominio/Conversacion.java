package com.trotamundos.asistente.dominio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Una conversación de un usuario con el asistente. */
public class Conversacion {

    private final UUID id;
    private final String usuarioId;
    private final Instant iniciadaEn;
    private final List<Mensaje> mensajes = new ArrayList<>();

    public Conversacion(String usuarioId) {
        this.id = UUID.randomUUID();
        this.usuarioId = usuarioId;
        this.iniciadaEn = Instant.now();
    }

    public synchronized void agregar(Autor autor, String texto) {
        mensajes.add(new Mensaje(autor, texto, Instant.now()));
    }

    /** Últimos mensajes de la conversación: lo único que se envía al modelo (decisión 4.1 de la Iteración 6). */
    public synchronized List<Mensaje> ultimosMensajes(int cantidad) {
        int desde = Math.max(0, mensajes.size() - cantidad);
        return List.copyOf(mensajes.subList(desde, mensajes.size()));
    }

    public synchronized int cantidadMensajes() {
        return mensajes.size();
    }

    public boolean perteneceA(String usuarioId) {
        return this.usuarioId.equals(usuarioId);
    }

    public UUID getId() {
        return id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public Instant getIniciadaEn() {
        return iniciadaEn;
    }
}
