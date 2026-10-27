package com.trotamundos.viajes.api;

import com.trotamundos.viajes.api.generado.ReservasApi;
import com.trotamundos.viajes.api.generado.modelo.NuevaReservaDto;
import com.trotamundos.viajes.api.generado.modelo.ReservaDto;
import com.trotamundos.viajes.dominio.FuenteReserva;
import com.trotamundos.viajes.dominio.Reserva;
import com.trotamundos.viajes.dominio.TipoReserva;
import com.trotamundos.viajes.servicio.ReservaService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Gestión manual de reservas (RF-12). Implementa la interfaz generada desde la spec. */
@RestController
public class ReservasController implements ReservasApi {

    private final ReservaService reservaService;

    public ReservasController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @Override
    public ResponseEntity<ReservaDto> crearReserva(String xUsuarioId, NuevaReservaDto nuevaReserva) {
        Reserva reserva = new Reserva(
                TipoReserva.valueOf(nuevaReserva.getTipo().getValue()),
                nuevaReserva.getProveedor(),
                nuevaReserva.getCodigo(),
                MapeoDto.aFechaUtc(nuevaReserva.getFechaInicio()),
                MapeoDto.aFechaUtc(nuevaReserva.getFechaFin()),
                nuevaReserva.getOrigen(),
                nuevaReserva.getDestino(),
                FuenteReserva.MANUAL,
                nuevaReserva.getDetalles());
        Reserva creada = reservaService.registrar(xUsuarioId, reserva);
        return ResponseEntity.status(HttpStatus.CREATED).body(MapeoDto.reservaADto(creada));
    }

    @Override
    public ResponseEntity<Void> eliminarReserva(String xUsuarioId, UUID id) {
        reservaService.eliminar(xUsuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
