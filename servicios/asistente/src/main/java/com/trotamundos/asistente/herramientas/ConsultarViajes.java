package com.trotamundos.asistente.herramientas;

import com.trotamundos.asistente.integracion.ClienteViajes;
import com.trotamundos.asistente.integracion.ReservaResumen;
import com.trotamundos.asistente.integracion.ViajeResumen;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Herramienta consultarViajes → Viajes: GET /v1/viajes (RF-24; Iteración 5, tabla 5.2). */
@Component
public class ConsultarViajes implements Herramienta {

    /**
     * Al modelo no se le envían los códigos de reserva ni los detalles (asiento, pasajeros...):
     * con un código y un apellido se puede gestionar una reserva en el sitio del proveedor (ADR 019).
     */
    private static final Set<String> CAMPOS_PERMITIDOS = Set.of(
            "viajes", "nombre", "destino", "origen", "fechaInicio", "fechaFin",
            "reservas", "tipo", "proveedor", "estado");

    private final ClienteViajes clienteViajes;

    public ConsultarViajes(ClienteViajes clienteViajes) {
        this.clienteViajes = clienteViajes;
    }

    @Override
    public String nombre() {
        return "consultarViajes";
    }

    @Override
    public String descripcion() {
        return "Consulta los viajes activos del usuario, con sus reservas y su estado actual.";
    }

    @Override
    public Set<String> parametrosPermitidos() {
        return Set.of();
    }

    @Override
    public Set<String> camposPermitidos() {
        return CAMPOS_PERMITIDOS;
    }

    @Override
    public boolean requiereConfirmacion() {
        return false;
    }

    @Override
    public ResultadoHerramienta ejecutar(Map<String, Object> parametros, ContextoEjecucion contexto) {
        List<Map<String, Object>> viajes = clienteViajes.viajesActivos(contexto.usuarioId()).stream()
                .map(ConsultarViajes::viajeAMapa)
                .toList();
        return ResultadoHerramienta.conDatos(Map.of("viajes", viajes));
    }

    private static Map<String, Object> viajeAMapa(ViajeResumen viaje) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("nombre", viaje.nombre());
        mapa.put("destino", viaje.destino());
        mapa.put("fechaInicio", String.valueOf(viaje.fechaInicio()));
        mapa.put("fechaFin", String.valueOf(viaje.fechaFin()));
        mapa.put("reservas", viaje.reservas() == null ? List.of()
                : viaje.reservas().stream().map(ConsultarViajes::reservaAMapa).toList());
        return mapa;
    }

    private static Map<String, Object> reservaAMapa(ReservaResumen reserva) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("tipo", reserva.tipo());
        mapa.put("proveedor", reserva.proveedor());
        mapa.put("codigo", reserva.codigo());
        mapa.put("fechaInicio", String.valueOf(reserva.fechaInicio()));
        mapa.put("fechaFin", String.valueOf(reserva.fechaFin()));
        mapa.put("origen", reserva.origen());
        mapa.put("destino", reserva.destino());
        mapa.put("estado", reserva.estado());
        mapa.put("detalles", reserva.detalles());
        return mapa;
    }
}
