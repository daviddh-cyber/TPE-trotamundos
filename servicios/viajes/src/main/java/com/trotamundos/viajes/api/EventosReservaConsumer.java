package com.trotamundos.viajes.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trotamundos.viajes.eventos.EventoDominio;
import com.trotamundos.viajes.eventos.Topicos;
import com.trotamundos.viajes.servicio.ReservaService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Consume ReservaActualizada y ReservaCancelada publicados por Proveedores (diagrama de secuencia, paso 10). */
@Component
public class EventosReservaConsumer {

    private final ObjectMapper objectMapper;
    private final ReservaService reservaService;

    public EventosReservaConsumer(ObjectMapper objectMapper, ReservaService reservaService) {
        this.objectMapper = objectMapper;
        this.reservaService = reservaService;
    }

    @KafkaListener(topics = Topicos.CAMBIOS_RESERVA, groupId = "viajes")
    public void alRecibirCambioDeReserva(String mensaje) throws JsonProcessingException {
        EventoDominio evento = objectMapper.readValue(mensaje, EventoDominio.class);
        reservaService.aplicarCambioDeProveedor(evento);
    }
}
