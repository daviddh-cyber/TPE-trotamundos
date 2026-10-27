package com.trotamundos.proveedores.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trotamundos.proveedores.eventos.EventoDominio;
import com.trotamundos.proveedores.eventos.Topicos;
import com.trotamundos.proveedores.servicio.MonitoreoService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Consume ReservaRegistrada para empezar a monitorear cada reserva nueva (Iteración 2, sección 5.4). */
@Component
public class EventosReservaConsumer {

    private final ObjectMapper objectMapper;
    private final MonitoreoService monitoreoService;

    public EventosReservaConsumer(ObjectMapper objectMapper, MonitoreoService monitoreoService) {
        this.objectMapper = objectMapper;
        this.monitoreoService = monitoreoService;
    }

    @KafkaListener(topics = Topicos.RESERVAS_REGISTRADAS, groupId = "proveedores")
    public void alRegistrarseReserva(String mensaje) throws JsonProcessingException {
        EventoDominio evento = objectMapper.readValue(mensaje, EventoDominio.class);
        monitoreoService.registrarMonitoreo(evento);
    }
}
