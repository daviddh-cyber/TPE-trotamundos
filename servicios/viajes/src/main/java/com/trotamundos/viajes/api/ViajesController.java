package com.trotamundos.viajes.api;

import com.trotamundos.viajes.api.generado.ViajesApi;
import com.trotamundos.viajes.api.generado.modelo.ViajeDto;
import com.trotamundos.viajes.servicio.ConsultaViajesService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Implementa la interfaz generada desde specs/viajes-api.yaml (R-3). */
@RestController
public class ViajesController implements ViajesApi {

    private final ConsultaViajesService consultaViajesService;

    public ViajesController(ConsultaViajesService consultaViajesService) {
        this.consultaViajesService = consultaViajesService;
    }

    @Override
    public ResponseEntity<List<ViajeDto>> listarViajes(String xUsuarioId) {
        List<ViajeDto> viajes = consultaViajesService.viajesActivos(xUsuarioId).stream()
                .map(MapeoDto::viajeADto)
                .toList();
        return ResponseEntity.ok(viajes);
    }

    @Override
    public ResponseEntity<ViajeDto> obtenerViaje(String xUsuarioId, UUID id) {
        return ResponseEntity.ok(MapeoDto.viajeADto(consultaViajesService.obtener(xUsuarioId, id)));
    }
}
