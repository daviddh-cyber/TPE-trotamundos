package com.trotamundos.proveedores.api;

import com.trotamundos.proveedores.api.generado.MonitoreoApi;
import com.trotamundos.proveedores.api.generado.modelo.ReservaMonitoreadaDto;
import com.trotamundos.proveedores.servicio.MonitoreoService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Lista las reservas monitoreadas (apoyo para la demostración del prototipo). */
@RestController
public class MonitoreoController implements MonitoreoApi {

    private final MonitoreoService monitoreoService;

    public MonitoreoController(MonitoreoService monitoreoService) {
        this.monitoreoService = monitoreoService;
    }

    @Override
    public ResponseEntity<List<ReservaMonitoreadaDto>> listarReservasMonitoreadas() {
        List<ReservaMonitoreadaDto> reservas = monitoreoService.reservasMonitoreadas().stream()
                .map(r -> new ReservaMonitoreadaDto()
                        .reservaId(r.getReservaId())
                        .usuarioId(r.getUsuarioId())
                        .proveedor(r.getProveedor())
                        .codigo(r.getCodigo()))
                .toList();
        return ResponseEntity.ok(reservas);
    }
}
