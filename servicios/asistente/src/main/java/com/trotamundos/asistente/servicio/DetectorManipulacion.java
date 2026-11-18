package com.trotamundos.asistente.servicio;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Reconoce frases típicas de manipulación en los mensajes del usuario (SE-4; Iteración 6, decisión 4.4).
 * Es la señal 1 de las cuatro; las otras las detecta el orquestador al validar las propuestas del modelo.
 * Un falso positivo solo genera un registro: nunca bloquea un mensaje por sí solo.
 */
@Component
public class DetectorManipulacion {

    private static final List<Pattern> FRASES = List.of(
            Pattern.compile("(?iu)ignor\\S*\\s+(tus\\s+|las\\s+|todas\\s+las\\s+)?(instrucciones|reglas)"),
            Pattern.compile("(?iu)(reservas|viajes|datos)\\s+de\\s+(otro\\s+usuario|otra\\s+persona|usuario-[\\w-]+)"),
            Pattern.compile("(?iu)(sos|actu[aá]\\s+como|modo)\\s+(administrador|admin|desarrollador)"),
            Pattern.compile("(?iu)(system\\s+prompt|instrucciones\\s+del\\s+sistema)"));

    public List<String> analizar(String texto) {
        List<String> senales = new ArrayList<>();
        for (Pattern frase : FRASES) {
            Matcher coincidencia = frase.matcher(texto);
            if (coincidencia.find()) {
                senales.add("frase típica de manipulación: \"" + coincidencia.group() + "\"");
            }
        }
        return senales;
    }
}
