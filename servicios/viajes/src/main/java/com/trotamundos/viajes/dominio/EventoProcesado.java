package com.trotamundos.viajes.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Registro de los eventos ya procesados. Kafka entrega cada evento al menos una vez,
 * así que se guarda su eventoId para descartar repetidos (idempotencia, Iteración 2, sección 5.6).
 */
@Entity
@Table(name = "eventos_procesados")
public class EventoProcesado {

    @Id
    private String eventoId;

    private Instant procesadoEn;

    protected EventoProcesado() {
        // requerido por JPA
    }

    public EventoProcesado(String eventoId) {
        this.eventoId = eventoId;
        this.procesadoEn = Instant.now();
    }

    public String getEventoId() {
        return eventoId;
    }

    public Instant getProcesadoEn() {
        return procesadoEn;
    }
}
