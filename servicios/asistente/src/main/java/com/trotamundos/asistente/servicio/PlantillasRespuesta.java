package com.trotamundos.asistente.servicio;

import com.trotamundos.asistente.integracion.ReservaResumen;
import com.trotamundos.asistente.integracion.ViajeResumen;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Arma las respuestas de estado con los datos de Viajes, sin LLM (ADR 017).
 * Los datos se muestran tal como están en Viajes: el modelo no puede afirmar datos incorrectos (RI-8).
 * Como esta respuesta no pasa por el LLM, puede incluir los códigos de reserva.
 */
@Component
public class PlantillasRespuesta {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM 'a las' HH:mm");

    public String estadoDeViajes(List<ViajeResumen> viajes) {
        if (viajes.isEmpty()) {
            return "No tenés viajes activos en tu panel.";
        }
        StringBuilder texto = new StringBuilder("Este es el estado de tus viajes:");
        for (ViajeResumen viaje : viajes) {
            texto.append("\n\n").append(viaje.nombre()).append(":");
            for (ReservaResumen reserva : viaje.reservas() == null ? List.<ReservaResumen>of() : viaje.reservas()) {
                texto.append("\n- ").append(describir(reserva));
            }
        }
        texto.append("\n\n¿Necesitás algo más? También puedo derivar tu caso a tu agencia de viajes.");
        return texto.toString();
    }

    private static String describir(ReservaResumen reserva) {
        Map<String, Object> detalles = reserva.detalles() == null ? Map.of() : reserva.detalles();
        StringBuilder texto = new StringBuilder(tipo(reserva.tipo()));
        if (detalles.get("numeroVuelo") != null) {
            texto.append(" ").append(detalles.get("numeroVuelo"));
        }
        texto.append(" (").append(reserva.proveedor()).append(", reserva ").append(reserva.codigo()).append(")");
        if (reserva.origen() != null) {
            texto.append(" ").append(reserva.origen()).append(" → ").append(reserva.destino());
        } else {
            texto.append(" en ").append(reserva.destino());
        }
        texto.append(": ").append(fecha(reserva.fechaInicio())).append(" (UTC)");
        if (detalles.get("puerta") != null) {
            texto.append(", puerta ").append(detalles.get("puerta"));
        }
        texto.append(". Estado: ").append(estado(reserva.estado())).append(".");
        return texto.toString();
    }

    private static String fecha(OffsetDateTime fecha) {
        return fecha == null ? "sin fecha" : fecha.withOffsetSameInstant(ZoneOffset.UTC).format(FECHA_HORA);
    }

    private static String tipo(String tipo) {
        return switch (String.valueOf(tipo)) {
            case "VUELO" -> "Vuelo";
            case "HOTEL" -> "Hotel";
            case "AUTO" -> "Auto de alquiler";
            default -> "Reserva";
        };
    }

    private static String estado(String estado) {
        return switch (String.valueOf(estado)) {
            case "CONFIRMADA" -> "confirmada";
            case "CANCELADA" -> "CANCELADA";
            case "PENDIENTE_REVISION" -> "pendiente de revisión";
            default -> String.valueOf(estado);
        };
    }
}
