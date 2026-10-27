package com.trotamundos.proveedores.servicio;

import com.trotamundos.proveedores.config.ClavesProveedoresProperties;
import com.trotamundos.proveedores.datos.EventoPublisher;
import com.trotamundos.proveedores.datos.ReservaMonitoreadaRepository;
import com.trotamundos.proveedores.dominio.ReservaMonitoreada;
import com.trotamundos.proveedores.eventos.EventoDominio;
import com.trotamundos.proveedores.eventos.Topicos;
import com.trotamundos.proveedores.integracion.AdaptadorProveedor;
import com.trotamundos.proveedores.integracion.CambioReserva;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Monitorea las reservas y traduce los cambios de los proveedores al modelo común.
 * Ver el diagrama de secuencia de la Iteración 2, pasos 3 a 7.
 */
@Service
public class MonitoreoService {

    private static final Logger log = LoggerFactory.getLogger(MonitoreoService.class);

    private final Map<String, AdaptadorProveedor> adaptadores;
    private final ReservaMonitoreadaRepository repositorio;
    private final EventoPublisher eventoPublisher;
    private final ClavesProveedoresProperties claves;

    public MonitoreoService(List<AdaptadorProveedor> adaptadores,
                            ReservaMonitoreadaRepository repositorio,
                            EventoPublisher eventoPublisher,
                            ClavesProveedoresProperties claves) {
        // Spring inyecta todos los adaptadores disponibles: sumar un proveedor no requiere tocar esta clase (IN-1).
        this.adaptadores = adaptadores.stream()
                .collect(Collectors.toMap(AdaptadorProveedor::proveedor, Function.identity()));
        this.repositorio = repositorio;
        this.eventoPublisher = eventoPublisher;
        this.claves = claves;
        log.info("Adaptadores de proveedores disponibles: {}", this.adaptadores.keySet());
    }

    /** Al recibir ReservaRegistrada, empieza a monitorear la reserva (RF-08). */
    @Transactional
    public void registrarMonitoreo(EventoDominio evento) {
        UUID reservaId = UUID.fromString(String.valueOf(evento.datos().get("reservaId")));
        if (repositorio.existsById(reservaId)) {
            log.info("La reserva {} ya se monitorea: evento repetido descartado", reservaId);
            return;
        }
        String proveedor = String.valueOf(evento.datos().get("proveedor"));
        String codigo = String.valueOf(evento.datos().get("codigo"));
        repositorio.save(new ReservaMonitoreada(reservaId, evento.usuarioId(), proveedor, codigo));
        // En producción, acá se consultarían los detalles de la reserva a la API del proveedor (RF-08).
        // En el prototipo los proveedores son simulados.
        log.info("Monitoreando la reserva {}:{} del usuario {}", proveedor, codigo, evento.usuarioId());
    }

    /** Procesa una notificación recibida por webhook (RF-09). */
    @Transactional(readOnly = true)
    public void procesarNotificacion(String proveedor, String clave, Map<String, Object> notificacion) {
        AdaptadorProveedor adaptador = adaptadores.get(proveedor);
        if (adaptador == null) {
            throw new ProveedorDesconocidoException(proveedor);
        }
        if (!clave.equals(claves.claves().get(proveedor))) {
            throw new ClaveInvalidaException(proveedor);
        }

        // Paso 4: el adaptador traduce el formato del proveedor al modelo común.
        CambioReserva cambio = adaptador.traducir(notificacion);

        // Paso 6: se busca a quién pertenece la reserva (puede estar compartida por varios usuarios).
        List<ReservaMonitoreada> monitoreadas = repositorio.findByProveedorAndCodigo(proveedor, cambio.codigo());
        if (monitoreadas.isEmpty()) {
            log.warn("Notificación de {} para una reserva no monitoreada ({}); se ignora", proveedor, cambio.codigo());
            return;
        }

        // Paso 7: se publica el evento, con clave proveedor:código para mantener el orden.
        for (ReservaMonitoreada reserva : monitoreadas) {
            Map<String, Object> datos = new HashMap<>();
            datos.put("proveedor", proveedor);
            datos.put("codigo", cambio.codigo());
            String tipo;
            if (cambio.tipo() == CambioReserva.Tipo.CANCELACION) {
                tipo = "ReservaCancelada";
                datos.put("motivo", cambio.motivo());
            } else {
                tipo = "ReservaActualizada";
                datos.put("cambios", cambio.cambios());
            }
            eventoPublisher.publicar(Topicos.CAMBIOS_RESERVA, proveedor + ":" + cambio.codigo(),
                    EventoDominio.nuevo(tipo, reserva.getUsuarioId(), datos));
        }
    }

    @Transactional(readOnly = true)
    public List<ReservaMonitoreada> reservasMonitoreadas() {
        return repositorio.findAll();
    }
}
