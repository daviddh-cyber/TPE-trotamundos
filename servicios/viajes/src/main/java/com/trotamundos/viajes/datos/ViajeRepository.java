package com.trotamundos.viajes.datos;

import com.trotamundos.viajes.dominio.EstadoViaje;
import com.trotamundos.viajes.dominio.Viaje;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViajeRepository extends JpaRepository<Viaje, UUID> {

    List<Viaje> findByUsuarioIdAndEstadoOrderByFechaInicioAsc(String usuarioId, EstadoViaje estado);

    Optional<Viaje> findByIdAndUsuarioId(UUID id, String usuarioId);

    List<Viaje> findByEstadoAndFechaFinBefore(EstadoViaje estado, LocalDate fecha);
}
