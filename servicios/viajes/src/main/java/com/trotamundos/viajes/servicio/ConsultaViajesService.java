package com.trotamundos.viajes.servicio;

import com.trotamundos.viajes.datos.ViajeRepository;
import com.trotamundos.viajes.dominio.EstadoViaje;
import com.trotamundos.viajes.dominio.Viaje;
import java.util.List;
import java.util.UUID;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consultas del panel del viajero (RF-13, RE-3). */
@Service
public class ConsultaViajesService {

    private final ViajeRepository viajeRepository;

    public ConsultaViajesService(ViajeRepository viajeRepository) {
        this.viajeRepository = viajeRepository;
    }

    @Transactional(readOnly = true)
    public List<Viaje> viajesActivos(String usuarioId) {
        List<Viaje> viajes = viajeRepository.findByUsuarioIdAndEstadoOrderByFechaInicioAsc(usuarioId, EstadoViaje.ACTIVO);
        viajes.forEach(v -> Hibernate.initialize(v.getReservas()));
        return viajes;
    }

    @Transactional(readOnly = true)
    public Viaje obtener(String usuarioId, UUID viajeId) {
        Viaje viaje = viajeRepository.findByIdAndUsuarioId(viajeId, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el viaje " + viajeId));
        Hibernate.initialize(viaje.getReservas());
        return viaje;
    }
}
