package com.trotamundos.asistente.datos;

import com.trotamundos.asistente.dominio.AccionPendiente;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Acciones que esperan confirmación. En memoria, como las conversaciones (R-4). */
@Component
public class AccionPendienteRepository {

    private final Map<UUID, AccionPendiente> acciones = new ConcurrentHashMap<>();

    public AccionPendiente guardar(AccionPendiente accion) {
        acciones.put(accion.getId(), accion);
        return accion;
    }

    /** Solo devuelve la acción si pertenece a ese usuario y a esa conversación. */
    public Optional<AccionPendiente> buscar(UUID id, String usuarioId, UUID conversacionId) {
        return Optional.ofNullable(acciones.get(id)).filter(a -> a.perteneceA(usuarioId, conversacionId));
    }
}
