package com.trotamundos.viajes.api;

import com.trotamundos.viajes.api.generado.modelo.EstadoReservaDto;
import com.trotamundos.viajes.api.generado.modelo.EstadoViajeDto;
import com.trotamundos.viajes.api.generado.modelo.ReservaDto;
import com.trotamundos.viajes.api.generado.modelo.TipoReservaDto;
import com.trotamundos.viajes.api.generado.modelo.ViajeDto;
import com.trotamundos.viajes.dominio.Reserva;
import com.trotamundos.viajes.dominio.Viaje;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** Traduce entre el modelo de dominio y los DTOs generados desde la spec OpenAPI. */
final class MapeoDto {

    private MapeoDto() {
    }

    static ViajeDto viajeADto(Viaje viaje) {
        return new ViajeDto()
                .id(viaje.getId())
                .nombre(viaje.getNombre())
                .destino(viaje.getDestino())
                .fechaInicio(viaje.getFechaInicio())
                .fechaFin(viaje.getFechaFin())
                .estado(EstadoViajeDto.fromValue(viaje.getEstado().name()))
                .reservas(viaje.getReservas().stream().map(MapeoDto::reservaADto).toList());
    }

    static ReservaDto reservaADto(Reserva reserva) {
        return new ReservaDto()
                .id(reserva.getId())
                .tipo(TipoReservaDto.fromValue(reserva.getTipo().name()))
                .proveedor(reserva.getProveedor())
                .codigo(reserva.getCodigo())
                .fechaInicio(aUtc(reserva.getFechaInicio()))
                .fechaFin(aUtc(reserva.getFechaFin()))
                .origen(reserva.getOrigen())
                .destino(reserva.getDestino())
                .estado(EstadoReservaDto.fromValue(reserva.getEstado().name()))
                .detalles(reserva.getDetalles())
                .ultimaActualizacion(reserva.getUltimaActualizacion() == null
                        ? null
                        : reserva.getUltimaActualizacion().atOffset(ZoneOffset.UTC));
    }

    static LocalDateTime aFechaUtc(OffsetDateTime fecha) {
        return fecha.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private static OffsetDateTime aUtc(LocalDateTime fecha) {
        return fecha.atOffset(ZoneOffset.UTC);
    }
}
