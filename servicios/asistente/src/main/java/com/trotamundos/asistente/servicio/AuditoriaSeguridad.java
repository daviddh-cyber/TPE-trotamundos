package com.trotamundos.asistente.servicio;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Registra cada intento de manipulación y reacciona ante un usuario que insiste
 * (SE-4; ADR 019, barrera 9; Iteración 6, sección 5.3).
 * En producción, el registro va a la plataforma de observabilidad y se conserva 1 año;
 * en el prototipo, a los logs del contenedor.
 */
@Component
public class AuditoriaSeguridad {

    static final int INTENTOS_PARA_BLOQUEO = 3;
    static final Duration VENTANA = Duration.ofHours(1);
    static final Duration BLOQUEO = Duration.ofHours(1);

    private static final Logger log = LoggerFactory.getLogger(AuditoriaSeguridad.class);

    private final Map<String, Deque<Instant>> intentos = new ConcurrentHashMap<>();
    private final Map<String, Instant> bloqueadosHasta = new ConcurrentHashMap<>();

    public void registrarIntento(String usuarioId, UUID conversacionId, String mensaje, List<String> senales) {
        log.warn("AUDITORIA | intento de manipulación | usuario={} | conversacion={} | señales={} | mensaje=\"{}\"",
                usuarioId, conversacionId, senales, mensaje);

        Instant ahora = Instant.now();
        Deque<Instant> recientes = intentos.computeIfAbsent(usuarioId, u -> new ArrayDeque<>());
        synchronized (recientes) {
            recientes.addLast(ahora);
            while (!recientes.isEmpty() && recientes.peekFirst().isBefore(ahora.minus(VENTANA))) {
                recientes.pollFirst();
            }
            if (recientes.size() >= INTENTOS_PARA_BLOQUEO) {
                recientes.clear();
                Instant hasta = ahora.plus(BLOQUEO);
                bloqueadosHasta.put(usuarioId, hasta);
                log.error("AUDITORIA | ALERTA al equipo de seguridad | usuario={} | {} intentos en 1 hora "
                        + "| asistente bloqueado hasta {}", usuarioId, INTENTOS_PARA_BLOQUEO, hasta);
            }
        }
    }

    public boolean estaBloqueado(String usuarioId) {
        Instant hasta = bloqueadosHasta.get(usuarioId);
        return hasta != null && Instant.now().isBefore(hasta);
    }
}
