package com.trotamundos.asistente.integracion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Implementación de ModeloLenguaje sin LLM: reconoce las consultas más comunes con palabras clave.
 * Tiene dos usos (Iteración 6, sección 5.1):
 * 1. Clasificar los mensajes para el camino rápido (ADR 017).
 * 2. Responder en modo degradado si el LLM falla (ADR 018).
 * En el prototipo es la única implementación (deuda técnica DT-4).
 */
@Component
public class MotorReglas implements ModeloLenguaje {

    static final String AYUDA = "Puedo contarte el estado de tus viajes o derivar tu caso a tu agencia de viajes. "
            + "¿En qué te ayudo?";

    private static final Pattern DERIVAR = Pattern.compile(
            "(?iu)(agencia|reembolso|devoluci[oó]n|reclamo|hablar con (una )?persona|humano)");
    private static final Pattern ESTADO = Pattern.compile(
            "(?iu)(estado|c[oó]mo (est[aá]|va)|mis? (vuelos?|viajes?|reservas?|hotel)|a qu[eé] hora|horario|puerta)");
    private static final Pattern VIAJES = Pattern.compile("(?iu)(viaje|vuelo|reserva|hotel|auto)");
    private static final Pattern OTRO_USUARIO = Pattern.compile("(?iu)\\b(usuario-[\\w-]+)");

    /** Camino rápido: solo si reconoce con seguridad una consulta de estado (ADR 017). */
    public boolean esConsultaDeEstado(String texto) {
        return ESTADO.matcher(texto).find() && !DERIVAR.matcher(texto).find();
    }

    @Override
    public PropuestaHerramientas interpretar(ContextoConversacion contexto, List<DescripcionHerramienta> disponibles) {
        String texto = contexto.ultimoMensajeDelUsuario();
        List<LlamadaHerramienta> llamadas = new ArrayList<>();

        if (DERIVAR.matcher(texto).find()) {
            llamadas.add(new LlamadaHerramienta("derivarCaso", Map.of("motivo", recortar(texto, 200))));
        } else {
            Matcher otroUsuario = OTRO_USUARIO.matcher(texto);
            if (otroUsuario.find()) {
                // Simula un modelo engañado por prompt injection: propone consultar los datos del usuario
                // que nombra el mensaje. Sirve para demostrar que la seguridad no depende del modelo:
                // el orquestador rechaza la propuesta (ADR 015, ADR 019).
                llamadas.add(new LlamadaHerramienta("consultarViajes", Map.of("usuarioId", otroUsuario.group(1))));
            } else if (VIAJES.matcher(texto).find()) {
                llamadas.add(new LlamadaHerramienta("consultarViajes", Map.of()));
            }
        }

        List<String> nombres = disponibles.stream().map(DescripcionHerramienta::nombre).toList();
        return new PropuestaHerramientas(llamadas.stream().filter(l -> nombres.contains(l.herramienta())).toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public String redactar(ContextoConversacion contexto, Map<String, Object> datosHerramientas) {
        Object viajes = datosHerramientas.get("viajes");
        if (!(viajes instanceof List<?> lista)) {
            return AYUDA;
        }
        if (lista.isEmpty()) {
            return "No encontré viajes activos en tu panel.";
        }
        StringBuilder texto = new StringBuilder("Encontré " + lista.size()
                + (lista.size() == 1 ? " viaje activo:" : " viajes activos:"));
        for (Object elemento : lista) {
            Map<String, Object> viaje = (Map<String, Object>) elemento;
            List<?> reservas = (List<?>) viaje.getOrDefault("reservas", List.of());
            texto.append("\n- ").append(viaje.get("nombre"))
                    .append(", del ").append(viaje.get("fechaInicio"))
                    .append(" al ").append(viaje.get("fechaFin"))
                    .append(", con ").append(reservas.size())
                    .append(reservas.size() == 1 ? " reserva." : " reservas.");
        }
        return texto.toString();
    }

    private static String recortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo);
    }
}
