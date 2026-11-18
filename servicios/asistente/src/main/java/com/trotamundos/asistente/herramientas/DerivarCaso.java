package com.trotamundos.asistente.herramientas;

import com.trotamundos.asistente.datos.EventoPublisher;
import com.trotamundos.asistente.dominio.Autor;
import com.trotamundos.asistente.dominio.Mensaje;
import com.trotamundos.asistente.eventos.EventoDominio;
import com.trotamundos.asistente.eventos.Topicos;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Herramienta derivarCaso → publica el evento CasoDerivado, que procesa Comunicaciones
 * (RF-27, US-4; Iteración 5, decisión 4.5). Requiere confirmación: comparte datos con la agencia.
 */
@Component
public class DerivarCaso implements Herramienta {

    private final EventoPublisher eventoPublisher;

    public DerivarCaso(EventoPublisher eventoPublisher) {
        this.eventoPublisher = eventoPublisher;
    }

    @Override
    public String nombre() {
        return "derivarCaso";
    }

    @Override
    public String descripcion() {
        return "Deriva el caso del usuario a su agencia de viajes preferida, con un resumen de la conversación.";
    }

    @Override
    public Set<String> parametrosPermitidos() {
        return Set.of("motivo");
    }

    @Override
    public Set<String> camposPermitidos() {
        return Set.of();
    }

    @Override
    public boolean requiereConfirmacion() {
        return true;
    }

    @Override
    public String describirAccion(Map<String, Object> parametros) {
        return "Voy a derivar tu caso a tu agencia de viajes, con un resumen de esta conversación. "
                + "Motivo: \"" + parametros.getOrDefault("motivo", "") + "\".";
    }

    @Override
    public ResultadoHerramienta ejecutar(Map<String, Object> parametros, ContextoEjecucion contexto) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("conversacionId", contexto.conversacion().getId().toString());
        datos.put("motivo", parametros.getOrDefault("motivo", ""));
        datos.put("resumen", resumen(contexto));

        // Clave de partición: el usuario (Iteración 5, tabla 5.3).
        eventoPublisher.publicar(Topicos.CASOS_DERIVADOS, contexto.usuarioId(),
                EventoDominio.nuevo("CasoDerivado", contexto.usuarioId(), datos));

        return ResultadoHerramienta.conMensaje("Listo: derivé tu caso a tu agencia de viajes. "
                + "Te van a contactar por los canales habituales.");
    }

    /** Resumen para la agencia: los últimos mensajes del usuario (US-4). */
    private static String resumen(ContextoEjecucion contexto) {
        return contexto.conversacion().ultimosMensajes(10).stream()
                .filter(m -> m.autor() == Autor.USUARIO)
                .map(Mensaje::texto)
                .collect(Collectors.joining(" | "));
    }
}
