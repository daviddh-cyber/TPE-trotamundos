package com.trotamundos.asistente.servicio;

import com.trotamundos.asistente.datos.AccionPendienteRepository;
import com.trotamundos.asistente.dominio.AccionPendiente;
import com.trotamundos.asistente.dominio.Conversacion;
import com.trotamundos.asistente.herramientas.Herramienta;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Retiene las acciones que modifican algo hasta que el usuario las confirma (RF-28, SE-5).
 * La confirmación es otra petición HTTP, enviada por la aplicación cuando el usuario toca
 * el botón: escribir "sí, confirmo" en el chat no ejecuta nada (ADR 019, barrera 8).
 */
@Component
public class PoliticaConfirmacion {

    private final AccionPendienteRepository acciones;

    public PoliticaConfirmacion(AccionPendienteRepository acciones) {
        this.acciones = acciones;
    }

    public AccionPendiente retener(Conversacion conversacion, Herramienta herramienta, Map<String, Object> parametros) {
        return acciones.guardar(new AccionPendiente(conversacion.getId(), conversacion.getUsuarioId(),
                herramienta.nombre(), parametros, herramienta.describirAccion(parametros)));
    }

    public AccionPendiente resolver(String usuarioId, UUID conversacionId, UUID accionId, boolean confirmada) {
        AccionPendiente accion = acciones.buscar(accionId, usuarioId, conversacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la acción " + accionId));
        if (!accion.estaVigente(Instant.now())) {
            throw new AccionNoDisponibleException("La acción ya fue resuelta o venció");
        }
        try {
            accion.resolver(confirmada);
        } catch (IllegalStateException e) {
            throw new AccionNoDisponibleException("La acción ya fue resuelta o venció");
        }
        return accion;
    }
}
