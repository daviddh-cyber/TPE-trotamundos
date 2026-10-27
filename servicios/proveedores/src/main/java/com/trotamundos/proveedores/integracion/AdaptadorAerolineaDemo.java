package com.trotamundos.proveedores.integracion;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Adaptador de una aerolínea simulada. Su formato:
 * <pre>
 * { "pnr": "ABC123", "evento": "SCHEDULE_CHANGE",
 *   "nuevaSalida": "2026-11-10T09:30:00Z", "nuevaLlegada": "2026-11-10T12:00:00Z", "puerta": "B12" }
 * { "pnr": "ABC123", "evento": "CANCELLED", "motivo": "Tormenta" }
 * </pre>
 */
@Component
public class AdaptadorAerolineaDemo implements AdaptadorProveedor {

    @Override
    public String proveedor() {
        return "aerolinea-demo";
    }

    @Override
    public CambioReserva traducir(Map<String, Object> notificacion) {
        String codigo = Notificaciones.requerido(notificacion, "pnr");
        String evento = Notificaciones.requerido(notificacion, "evento");
        return switch (evento) {
            case "SCHEDULE_CHANGE" -> {
                Map<String, Object> cambios = new HashMap<>();
                Notificaciones.copiar(notificacion, "nuevaSalida", cambios, "fechaInicio");
                Notificaciones.copiar(notificacion, "nuevaLlegada", cambios, "fechaFin");
                Notificaciones.copiar(notificacion, "puerta", cambios, "puerta");
                yield new CambioReserva(codigo, CambioReserva.Tipo.ACTUALIZACION, cambios, null);
            }
            case "CANCELLED" -> new CambioReserva(codigo, CambioReserva.Tipo.CANCELACION, Map.of(),
                    String.valueOf(notificacion.getOrDefault("motivo", "Sin motivo informado")));
            default -> throw new IllegalArgumentException("Evento desconocido de aerolinea-demo: " + evento);
        };
    }
}
