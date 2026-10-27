package com.trotamundos.viajes.datos;

import com.trotamundos.viajes.dominio.EventoProcesado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoProcesadoRepository extends JpaRepository<EventoProcesado, String> {
}
