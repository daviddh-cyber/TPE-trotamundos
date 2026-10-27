package com.trotamundos.proveedores.datos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trotamundos.proveedores.eventos.EventoDominio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Publica eventos en Kafka como JSON (ADR 002, ADR 004). */
@Component
public class EventoPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventoPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EventoPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * @param clave clave de partición: los eventos con la misma clave se entregan en orden.
     */
    public void publicar(String topico, String clave, EventoDominio evento) {
        try {
            kafkaTemplate.send(topico, clave, objectMapper.writeValueAsString(evento));
            log.info("Publicado {} en {} (clave {})", evento.tipo(), topico, clave);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el evento " + evento.tipo(), e);
        }
    }
}
