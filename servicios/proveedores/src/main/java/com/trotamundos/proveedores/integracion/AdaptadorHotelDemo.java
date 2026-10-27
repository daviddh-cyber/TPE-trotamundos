package com.trotamundos.proveedores.integracion;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Adaptador de un hotel simulado, con un formato distinto al de la aerolínea:
 * <pre>
 * { "bookingRef": "H-778", "status": "MODIFIED",
 *   "checkIn": "2026-11-10T14:00:00Z", "checkOut": "2026-11-15T10:00:00Z", "roomType": "Suite" }
 * { "bookingRef": "H-778", "status": "CANCELED", "reason": "Overbooking" }
 * </pre>
 */
@Component
public class AdaptadorHotelDemo implements AdaptadorProveedor {

    @Override
    public String proveedor() {
        return "hotel-demo";
    }

    @Override
    public CambioReserva traducir(Map<String, Object> notificacion) {
        String codigo = Notificaciones.requerido(notificacion, "bookingRef");
        String estado = Notificaciones.requerido(notificacion, "status");
        return switch (estado) {
            case "MODIFIED" -> {
                Map<String, Object> cambios = new HashMap<>();
                Notificaciones.copiar(notificacion, "checkIn", cambios, "fechaInicio");
                Notificaciones.copiar(notificacion, "checkOut", cambios, "fechaFin");
                Notificaciones.copiar(notificacion, "roomType", cambios, "tipoHabitacion");
                yield new CambioReserva(codigo, CambioReserva.Tipo.ACTUALIZACION, cambios, null);
            }
            case "CANCELED" -> new CambioReserva(codigo, CambioReserva.Tipo.CANCELACION, Map.of(),
                    String.valueOf(notificacion.getOrDefault("reason", "Sin motivo informado")));
            default -> throw new IllegalArgumentException("Estado desconocido de hotel-demo: " + estado);
        };
    }
}
