package com.trotamundos.asistente.integracion;

import com.trotamundos.asistente.dominio.Mensaje;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Revisa todo lo que se envía al modelo de lenguaje (SE-6; ADR 019, barreras 1 y 4).
 * 1. Campos permitidos: de los datos de cada herramienta solo pasan los campos que declara.
 * 2. Filtro de patrones: reemplaza correos, tokens y números de tarjeta por [DATO OCULTO].
 */
@Component
public class FiltroDatosSensibles {

    static final String OCULTO = "[DATO OCULTO]";

    private static final Logger log = LoggerFactory.getLogger(FiltroDatosSensibles.class);

    private static final List<Pattern> PATRONES = List.of(
            Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.-]+"),                 // correos electrónicos
            Pattern.compile("eyJ[\\w-]+\\.[\\w-]+\\.[\\w-]+"),                // tokens JWT
            Pattern.compile("(?i)bearer\\s+[\\w.~+/=-]+"),                    // tokens en headers
            Pattern.compile("\\b(?:\\d[ -]?){12,18}\\d\\b"));                 // números de tarjeta

    public String filtrar(String texto) {
        if (texto == null) {
            return null;
        }
        String resultado = texto;
        for (Pattern patron : PATRONES) {
            resultado = patron.matcher(resultado).replaceAll(OCULTO);
        }
        if (!resultado.equals(texto)) {
            log.info("Se ocultaron datos sensibles antes de consultar al modelo");
        }
        return resultado;
    }

    public ContextoConversacion filtrar(ContextoConversacion contexto) {
        return new ContextoConversacion(contexto.mensajes().stream()
                .map(m -> new Mensaje(m.autor(), filtrar(m.texto()), m.enviadoEn()))
                .toList());
    }

    /** Deja solo los campos permitidos, en cualquier nivel, y filtra los textos que quedan. */
    public Map<String, Object> soloCamposPermitidos(Map<String, Object> datos, Set<String> permitidos) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        datos.forEach((campo, valor) -> {
            if (permitidos.contains(campo)) {
                resultado.put(campo, filtrarValor(valor, permitidos));
            }
        });
        return resultado;
    }

    @SuppressWarnings("unchecked")
    private Object filtrarValor(Object valor, Set<String> permitidos) {
        if (valor instanceof Map<?, ?> mapa) {
            return soloCamposPermitidos((Map<String, Object>) mapa, permitidos);
        }
        if (valor instanceof List<?> lista) {
            return lista.stream().map(v -> filtrarValor(v, permitidos)).toList();
        }
        if (valor instanceof String texto) {
            return filtrar(texto);
        }
        return valor;
    }
}
