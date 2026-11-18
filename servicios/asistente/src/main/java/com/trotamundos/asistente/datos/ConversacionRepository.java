package com.trotamundos.asistente.datos;

import com.trotamundos.asistente.dominio.Conversacion;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Conversaciones del asistente.
 * Simplificación del prototipo (R-4): se guardan en memoria. En producción van en
 * una base PostgreSQL propia y se eliminan a los 90 días (Iteración 5, sección 5.4).
 */
@Component
public class ConversacionRepository {

    private final Map<UUID, Conversacion> conversaciones = new ConcurrentHashMap<>();

    public Conversacion guardar(Conversacion conversacion) {
        conversaciones.put(conversacion.getId(), conversacion);
        return conversacion;
    }

    /** Solo devuelve la conversación si pertenece al usuario. */
    public Optional<Conversacion> buscar(UUID id, String usuarioId) {
        return Optional.ofNullable(conversaciones.get(id)).filter(c -> c.perteneceA(usuarioId));
    }

    public List<Conversacion> deUsuario(String usuarioId) {
        return conversaciones.values().stream()
                .filter(c -> c.perteneceA(usuarioId))
                .sorted(Comparator.comparing(Conversacion::getIniciadaEn).reversed())
                .toList();
    }
}
