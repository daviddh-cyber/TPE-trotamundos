package com.trotamundos.asistente.servicio;

import com.trotamundos.asistente.datos.ConversacionRepository;
import com.trotamundos.asistente.dominio.AccionPendiente;
import com.trotamundos.asistente.dominio.Autor;
import com.trotamundos.asistente.dominio.Conversacion;
import com.trotamundos.asistente.herramientas.ContextoEjecucion;
import com.trotamundos.asistente.herramientas.Herramienta;
import com.trotamundos.asistente.herramientas.RegistroHerramientas;
import com.trotamundos.asistente.herramientas.ResultadoHerramienta;
import com.trotamundos.asistente.integracion.ClienteViajes;
import com.trotamundos.asistente.integracion.ContextoConversacion;
import com.trotamundos.asistente.integracion.FiltroDatosSensibles;
import com.trotamundos.asistente.integracion.LlamadaHerramienta;
import com.trotamundos.asistente.integracion.ModeloLenguaje;
import com.trotamundos.asistente.integracion.MotorReglas;
import com.trotamundos.asistente.integracion.PropuestaHerramientas;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * "El modelo propone, el orquestador decide" (ADR 015).
 * Recorrido de un mensaje (Iteración 6, diagrama 6.1):
 * 1. Camino rápido: si es una consulta de estado, responde con una plantilla, sin LLM (ADR 017).
 * 2. Si no, filtra el contexto y le pide al modelo que proponga herramientas (ADR 019).
 * 3. Valida cada propuesta: herramienta registrada y parámetros permitidos; si no, registra la señal.
 * 4. Las herramientas que modifican algo quedan pendientes de confirmación (RF-28).
 * 5. Las demás se ejecutan con el usuario autenticado, y el modelo redacta con los datos filtrados.
 */
@Service
public class Orquestador {

    static final int MENSAJES_DE_CONTEXTO = 10;
    static final String RECHAZO = "No puedo hacer eso: solo puedo consultar y gestionar tus propios viajes.";
    static final String NO_DISPONIBLE = "No pude consultar tus datos en este momento. "
            + "Si querés, puedo derivar tu caso a tu agencia de viajes.";

    private static final Logger log = LoggerFactory.getLogger(Orquestador.class);

    private final ConversacionRepository conversaciones;
    private final RegistroHerramientas herramientas;
    private final PoliticaConfirmacion politicaConfirmacion;
    private final ModeloLenguaje modelo;
    private final MotorReglas motorReglas;
    private final PlantillasRespuesta plantillas;
    private final ClienteViajes clienteViajes;
    private final FiltroDatosSensibles filtro;
    private final DetectorManipulacion detector;
    private final AuditoriaSeguridad auditoria;

    public Orquestador(ConversacionRepository conversaciones, RegistroHerramientas herramientas,
                       PoliticaConfirmacion politicaConfirmacion, ModeloLenguaje modelo, MotorReglas motorReglas,
                       PlantillasRespuesta plantillas, ClienteViajes clienteViajes, FiltroDatosSensibles filtro,
                       DetectorManipulacion detector, AuditoriaSeguridad auditoria) {
        this.conversaciones = conversaciones;
        this.herramientas = herramientas;
        this.politicaConfirmacion = politicaConfirmacion;
        this.modelo = modelo;
        this.motorReglas = motorReglas;
        this.plantillas = plantillas;
        this.clienteViajes = clienteViajes;
        this.filtro = filtro;
        this.detector = detector;
        this.auditoria = auditoria;
    }

    public Conversacion iniciar(String usuarioId) {
        return conversaciones.guardar(new Conversacion(usuarioId));
    }

    public List<Conversacion> historial(String usuarioId) {
        return conversaciones.deUsuario(usuarioId);
    }

    public Respuesta procesarMensaje(String usuarioId, UUID conversacionId, String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El mensaje está vacío");
        }
        if (auditoria.estaBloqueado(usuarioId)) {
            throw new UsuarioBloqueadoException("El asistente no está disponible para tu cuenta durante 1 hora");
        }
        Conversacion conversacion = buscarConversacion(usuarioId, conversacionId);
        conversacion.agregar(Autor.USUARIO, texto);

        List<String> senales = new ArrayList<>(detector.analizar(texto));
        Respuesta respuesta = motorReglas.esConsultaDeEstado(texto)
                ? caminoRapido(usuarioId)
                : conModelo(conversacion, senales);

        if (!senales.isEmpty()) {
            auditoria.registrarIntento(usuarioId, conversacionId, texto, senales);
        }
        conversacion.agregar(Autor.ASISTENTE, respuesta.texto());
        return respuesta;
    }

    public Respuesta confirmar(String usuarioId, UUID conversacionId, UUID accionId, boolean confirmada) {
        Conversacion conversacion = buscarConversacion(usuarioId, conversacionId);
        AccionPendiente accion = politicaConfirmacion.resolver(usuarioId, conversacionId, accionId, confirmada);

        String texto;
        if (!confirmada) {
            texto = "Listo, no hice ningún cambio.";
        } else {
            Herramienta herramienta = herramientas.buscar(accion.getHerramienta())
                    .orElseThrow(() -> new IllegalStateException("Herramienta desconocida: " + accion.getHerramienta()));
            ResultadoHerramienta resultado = herramienta.ejecutar(accion.getParametros(),
                    new ContextoEjecucion(usuarioId, conversacion));
            texto = resultado.mensajeParaUsuario();
        }
        conversacion.agregar(Autor.ASISTENTE, texto);
        return Respuesta.de(texto, OrigenRespuesta.ACCION);
    }

    /** ADR 017: las consultas de estado no llegan al LLM. */
    private Respuesta caminoRapido(String usuarioId) {
        try {
            return Respuesta.de(plantillas.estadoDeViajes(clienteViajes.viajesActivos(usuarioId)),
                    OrigenRespuesta.CAMINO_RAPIDO);
        } catch (RuntimeException e) {
            log.warn("No se pudo consultar Viajes: {}", e.getMessage());
            return Respuesta.de(NO_DISPONIBLE, OrigenRespuesta.CAMINO_RAPIDO);
        }
    }

    private Respuesta conModelo(Conversacion conversacion, List<String> senales) {
        // El modelo solo recibe los últimos mensajes, filtrados, y nunca el usuario ni su token.
        ContextoConversacion contexto = filtro.filtrar(
                new ContextoConversacion(conversacion.ultimosMensajes(MENSAJES_DE_CONTEXTO)));
        PropuestaHerramientas propuesta = modelo.interpretar(contexto, herramientas.descripciones());

        Map<String, Object> datos = new LinkedHashMap<>();
        boolean huboRechazos = false;
        for (LlamadaHerramienta llamada : propuesta.llamadas()) {
            Optional<Herramienta> encontrada = herramientas.buscar(llamada.herramienta());
            if (encontrada.isEmpty()) {
                senales.add("herramienta inexistente: " + llamada.herramienta());
                huboRechazos = true;
                continue;
            }
            Herramienta herramienta = encontrada.get();
            List<String> errores = herramienta.validar(llamada.parametros());
            if (!errores.isEmpty()) {
                senales.addAll(errores);
                huboRechazos = true;
                continue;
            }
            if (herramienta.requiereConfirmacion()) {
                AccionPendiente accion = politicaConfirmacion.retener(conversacion, herramienta, llamada.parametros());
                return new Respuesta(accion.getDescripcion() + " ¿Confirmás?", OrigenRespuesta.MODELO, accion);
            }
            try {
                ResultadoHerramienta resultado = herramienta.ejecutar(llamada.parametros(),
                        new ContextoEjecucion(conversacion.getUsuarioId(), conversacion));
                datos.putAll(filtro.soloCamposPermitidos(resultado.datos(), herramienta.camposPermitidos()));
            } catch (RuntimeException e) {
                log.warn("Falló la herramienta {}: {}", herramienta.nombre(), e.getMessage());
                return Respuesta.de(NO_DISPONIBLE, OrigenRespuesta.MODELO);
            }
        }

        if (huboRechazos && datos.isEmpty()) {
            return Respuesta.de(RECHAZO, OrigenRespuesta.MODELO);
        }
        return Respuesta.de(modelo.redactar(contexto, datos), OrigenRespuesta.MODELO);
    }

    private Conversacion buscarConversacion(String usuarioId, UUID conversacionId) {
        return conversaciones.buscar(conversacionId, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la conversación " + conversacionId));
    }
}
