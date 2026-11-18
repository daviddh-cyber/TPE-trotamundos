package com.trotamundos.asistente.integracion;

import com.trotamundos.asistente.dominio.Autor;
import com.trotamundos.asistente.dominio.Mensaje;
import java.util.List;

/**
 * Lo que recibe el modelo de lenguaje: los últimos mensajes, ya filtrados.
 * No incluye el usuario ni su token (Iteración 6, sección 5.2).
 */
public record ContextoConversacion(List<Mensaje> mensajes) {

    public String ultimoMensajeDelUsuario() {
        for (int i = mensajes.size() - 1; i >= 0; i--) {
            if (mensajes.get(i).autor() == Autor.USUARIO) {
                return mensajes.get(i).texto();
            }
        }
        return "";
    }
}
