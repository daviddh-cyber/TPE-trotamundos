package com.trotamundos.asistente.dominio;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Acción propuesta que modifica algo y espera la confirmación explícita del usuario
 * (RF-28, SE-5; Iteración 6, decisión 4.4).
 */
public class AccionPendiente {

    /** Una acción pendiente vence a los 10 minutos (Iteración 6, sección 5.3). */
    public static final Duration VIGENCIA = Duration.ofMinutes(10);

    private final UUID id;
    private final UUID conversacionId;
    private final String usuarioId;
    private final String herramienta;
    private final Map<String, Object> parametros;
    private final String descripcion;
    private final Instant venceEn;
    private EstadoAccion estado = EstadoAccion.PENDIENTE;

    public AccionPendiente(UUID conversacionId, String usuarioId, String herramienta,
                           Map<String, Object> parametros, String descripcion) {
        this.id = UUID.randomUUID();
        this.conversacionId = conversacionId;
        this.usuarioId = usuarioId;
        this.herramienta = herramienta;
        this.parametros = Map.copyOf(parametros);
        this.descripcion = descripcion;
        this.venceEn = Instant.now().plus(VIGENCIA);
    }

    public synchronized boolean estaVigente(Instant ahora) {
        return estado == EstadoAccion.PENDIENTE && ahora.isBefore(venceEn);
    }

    /** Marca la acción como confirmada o rechazada. Solo se puede resolver una vez. */
    public synchronized void resolver(boolean confirmada) {
        if (estado != EstadoAccion.PENDIENTE) {
            throw new IllegalStateException("La acción ya fue resuelta");
        }
        estado = confirmada ? EstadoAccion.CONFIRMADA : EstadoAccion.RECHAZADA;
    }

    public boolean perteneceA(String usuarioId, UUID conversacionId) {
        return this.usuarioId.equals(usuarioId) && this.conversacionId.equals(conversacionId);
    }

    public UUID getId() {
        return id;
    }

    public UUID getConversacionId() {
        return conversacionId;
    }

    public String getHerramienta() {
        return herramienta;
    }

    public Map<String, Object> getParametros() {
        return parametros;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Instant getVenceEn() {
        return venceEn;
    }

    public synchronized EstadoAccion getEstado() {
        return estado;
    }
}
