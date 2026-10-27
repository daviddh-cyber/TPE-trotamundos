package com.trotamundos.viajes.servicio;

import com.trotamundos.viajes.datos.ViajeRepository;
import com.trotamundos.viajes.dominio.EstadoViaje;
import com.trotamundos.viajes.dominio.Reserva;
import com.trotamundos.viajes.dominio.Viaje;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Agrupa reservas en viajes (RF-13) y archiva los viajes finalizados (RF-15). */
@Service
public class AgrupacionService {

    private static final Logger log = LoggerFactory.getLogger(AgrupacionService.class);

    /** Supuesto S-14: margen de días para considerar que una reserva pertenece a un viaje. */
    static final int MARGEN_DIAS = 2;

    private final ViajeRepository viajeRepository;

    public AgrupacionService(ViajeRepository viajeRepository) {
        this.viajeRepository = viajeRepository;
    }

    /**
     * Regla de agrupación (Iteración 2, sección 5.3): la reserva va a un viaje activo del usuario
     * si sus fechas se superponen o están a menos de MARGEN_DIAS días. Si no, se crea un viaje nuevo.
     */
    public Viaje asignarViaje(String usuarioId, Reserva reserva) {
        LocalDate inicio = reserva.getFechaInicio().toLocalDate();
        LocalDate fin = reserva.getFechaFin().toLocalDate();
        Viaje viaje = viajeRepository.findByUsuarioIdAndEstadoOrderByFechaInicioAsc(usuarioId, EstadoViaje.ACTIVO)
                .stream()
                .filter(v -> corresponde(v.getFechaInicio(), v.getFechaFin(), inicio, fin))
                .findFirst()
                .orElseGet(() -> new Viaje(usuarioId, nombreDeViaje(reserva), reserva.getDestino(), inicio, fin));
        viaje.agregarReserva(reserva);
        return viajeRepository.save(viaje);
    }

    static boolean corresponde(LocalDate viajeInicio, LocalDate viajeFin, LocalDate inicio, LocalDate fin) {
        return !inicio.isAfter(viajeFin.plusDays(MARGEN_DIAS))
                && !fin.isBefore(viajeInicio.minusDays(MARGEN_DIAS));
    }

    private static String nombreDeViaje(Reserva reserva) {
        return reserva.getDestino() == null ? "Viaje" : "Viaje a " + reserva.getDestino();
    }

    /** RF-15: todos los días a las 3 AM se archivan los viajes que ya terminaron. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void archivarViajesFinalizados() {
        var finalizados = viajeRepository.findByEstadoAndFechaFinBefore(EstadoViaje.ACTIVO, LocalDate.now());
        finalizados.forEach(Viaje::archivar);
        log.info("Viajes archivados: {}", finalizados.size());
    }
}
