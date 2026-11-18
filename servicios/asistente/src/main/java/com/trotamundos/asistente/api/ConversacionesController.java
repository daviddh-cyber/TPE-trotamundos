package com.trotamundos.asistente.api;

import com.trotamundos.asistente.api.generado.ConversacionesApi;
import com.trotamundos.asistente.api.generado.modelo.AccionPendienteDto;
import com.trotamundos.asistente.api.generado.modelo.ConfirmacionDto;
import com.trotamundos.asistente.api.generado.modelo.ConversacionDto;
import com.trotamundos.asistente.api.generado.modelo.NuevoMensajeDto;
import com.trotamundos.asistente.api.generado.modelo.OrigenRespuestaDto;
import com.trotamundos.asistente.api.generado.modelo.RespuestaAsistenteDto;
import com.trotamundos.asistente.dominio.AccionPendiente;
import com.trotamundos.asistente.dominio.Conversacion;
import com.trotamundos.asistente.servicio.Orquestador;
import com.trotamundos.asistente.servicio.Respuesta;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Implementa la interfaz generada desde specs/asistente-api.yaml (R-3). */
@RestController
public class ConversacionesController implements ConversacionesApi {

    private final Orquestador orquestador;

    public ConversacionesController(Orquestador orquestador) {
        this.orquestador = orquestador;
    }

    @Override
    public ResponseEntity<ConversacionDto> iniciarConversacion(String xUsuarioId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aDto(orquestador.iniciar(xUsuarioId)));
    }

    @Override
    public ResponseEntity<List<ConversacionDto>> listarConversaciones(String xUsuarioId) {
        return ResponseEntity.ok(orquestador.historial(xUsuarioId).stream().map(ConversacionesController::aDto).toList());
    }

    @Override
    public ResponseEntity<RespuestaAsistenteDto> enviarMensaje(String xUsuarioId, UUID id, NuevoMensajeDto nuevoMensajeDto) {
        return ResponseEntity.ok(aDto(orquestador.procesarMensaje(xUsuarioId, id, nuevoMensajeDto.getTexto())));
    }

    @Override
    public ResponseEntity<RespuestaAsistenteDto> confirmarAccion(String xUsuarioId, UUID id, UUID accionId,
                                                                 ConfirmacionDto confirmacionDto) {
        return ResponseEntity.ok(aDto(orquestador.confirmar(xUsuarioId, id, accionId,
                Boolean.TRUE.equals(confirmacionDto.getConfirmada()))));
    }

    private static ConversacionDto aDto(Conversacion conversacion) {
        return new ConversacionDto()
                .id(conversacion.getId())
                .iniciadaEn(conversacion.getIniciadaEn().atOffset(ZoneOffset.UTC))
                .cantidadMensajes(conversacion.cantidadMensajes());
    }

    private static RespuestaAsistenteDto aDto(Respuesta respuesta) {
        RespuestaAsistenteDto dto = new RespuestaAsistenteDto()
                .texto(respuesta.texto())
                .origen(OrigenRespuestaDto.fromValue(respuesta.origen().name()));
        AccionPendiente accion = respuesta.accionPendiente();
        if (accion != null) {
            dto.accionPendiente(new AccionPendienteDto()
                    .id(accion.getId())
                    .herramienta(accion.getHerramienta())
                    .descripcion(accion.getDescripcion())
                    .venceEn(accion.getVenceEn().atOffset(ZoneOffset.UTC)));
        }
        return dto;
    }
}
