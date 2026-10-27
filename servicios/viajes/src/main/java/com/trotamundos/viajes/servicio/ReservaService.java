package com.trotamundos.viajes.servicio;

import com.trotamundos.viajes.datos.EventoProcesadoRepository;
import com.trotamundos.viajes.datos.EventoPublisher;
import com.trotamundos.viajes.datos.ReservaRepository;
import com.trotamundos.viajes.dominio.EventoProcesado;
import com.trotamundos.viajes.dominio.Reserva;
import com.trotamundos.viajes.dominio.Viaje;
import com.trotamundos.viajes.eventos.EventoDominio;
import com.trotamundos.viajes.eventos.Topicos;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Alta, baja y actualización de reservas, sin importar de dónde vengan
 * (usuario, correo o proveedor). Iteración 2, sección 5.2.
 */
@Service
public class ReservaService {

    private static final Logger log = LoggerFactory.getLogger(ReservaService.class);

    private final ReservaRepository reservaRepository;
    private final EventoProcesadoRepository eventoProcesadoRepository;
    private final AgrupacionService agrupacionService;
    private final EventoPublisher eventoPublisher;

    public ReservaService(ReservaRepository reservaRepository,
                          EventoProcesadoRepository eventoProcesadoRepository,
                          AgrupacionService agrupacionService,
                          EventoPublisher eventoPublisher) {
        this.reservaRepository = reservaRepository;
        this.eventoProcesadoRepository = eventoProcesadoRepository;
        this.agrupacionService = agrupacionService;
        this.eventoPublisher = eventoPublisher;
    }

    /** Registra una reserva, la agrupa en un viaje y avisa a Proveedores para que la monitoree. */
    @Transactional
    public Reserva registrar(String usuarioId, Reserva reserva) {
        Viaje viaje = agrupacionService.asignarViaje(usuarioId, reserva);

        Map<String, Object> datos = new HashMap<>();
        datos.put("reservaId", reserva.getId().toString());
        datos.put("tipo", reserva.getTipo().name());
        datos.put("proveedor", reserva.getProveedor());
        datos.put("codigo", reserva.getCodigo());
        eventoPublisher.publicar(Topicos.RESERVAS_REGISTRADAS, clave(reserva),
                EventoDominio.nuevo("ReservaRegistrada", usuarioId, datos));

        publicarViajeActualizado(usuarioId, viaje, reserva, "RESERVA_AGREGADA");
        return reserva;
    }

    @Transactional
    public void eliminar(String usuarioId, UUID reservaId) {
        Reserva reserva = reservaRepository.findByIdAndViaje_UsuarioId(reservaId, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la reserva " + reservaId));
        Viaje viaje = reserva.getViaje();
        viaje.quitarReserva(reserva);
        publicarViajeActualizado(usuarioId, viaje, reserva, "RESERVA_ELIMINADA");
    }

    /**
     * Aplica un cambio informado por un proveedor (ReservaActualizada o ReservaCancelada).
     * Ver el diagrama de secuencia de la Iteración 2, pasos 11 a 14.
     */
    @Transactional
    public void aplicarCambioDeProveedor(EventoDominio evento) {
        // Paso 12: idempotencia. Si el evento ya se procesó, se descarta.
        if (eventoProcesadoRepository.existsById(evento.eventoId())) {
            log.info("Evento {} ya procesado: se descarta", evento.eventoId());
            return;
        }

        String proveedor = String.valueOf(evento.datos().get("proveedor"));
        String codigo = String.valueOf(evento.datos().get("codigo"));
        Optional<Reserva> encontrada = reservaRepository
                .findFirstByProveedorAndCodigoAndViaje_UsuarioId(proveedor, codigo, evento.usuarioId());

        if (encontrada.isEmpty()) {
            log.warn("{} para una reserva desconocida ({}:{}); se ignora", evento.tipo(), proveedor, codigo);
            eventoProcesadoRepository.save(new EventoProcesado(evento.eventoId()));
            return;
        }

        // Paso 13: se actualiza la reserva.
        Reserva reserva = encontrada.get();
        String cambio;
        if ("ReservaCancelada".equals(evento.tipo())) {
            reserva.cancelar();
            cambio = "RESERVA_CANCELADA";
        } else {
            reserva.aplicarActualizacion(cambiosDe(evento));
            cambio = "RESERVA_ACTUALIZADA";
        }
        reserva.getViaje().recalcularFechas();
        eventoProcesadoRepository.save(new EventoProcesado(evento.eventoId()));
        log.info("{} aplicada a la reserva {}:{}", evento.tipo(), proveedor, codigo);

        // Paso 14: se avisa que el viaje cambió.
        publicarViajeActualizado(evento.usuarioId(), reserva.getViaje(), reserva, cambio);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> cambiosDe(EventoDominio evento) {
        Object cambios = evento.datos().get("cambios");
        return cambios instanceof Map<?, ?> mapa ? (Map<String, Object>) mapa : Map.of();
    }

    private void publicarViajeActualizado(String usuarioId, Viaje viaje, Reserva reserva, String cambio) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("viajeId", viaje.getId().toString());
        datos.put("reservaId", reserva.getId().toString());
        datos.put("cambio", cambio);
        eventoPublisher.publicar(Topicos.VIAJES_ACTUALIZADOS, viaje.getId().toString(),
                EventoDominio.nuevo("ViajeActualizado", usuarioId, datos));
    }

    private static String clave(Reserva reserva) {
        return reserva.getProveedor() + ":" + reserva.getCodigo();
    }
}
