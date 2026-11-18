package com.trotamundos.asistente.integracion;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente de la API pública del servicio Viajes (ADR 014).
 * Siempre envía el usuario autenticado: el asistente ve exactamente lo mismo que el usuario
 * vería en su panel, y nada más (SE-4). En el prototipo, el usuario viaja en el header
 * X-Usuario-Id; en producción, como token del usuario.
 */
@Component
public class ClienteViajes {

    private final RestClient restClient;

    public ClienteViajes(RestClient.Builder builder, @Value("${trotamundos.viajes.url}") String urlViajes) {
        this.restClient = builder.baseUrl(urlViajes).build();
    }

    public List<ViajeResumen> viajesActivos(String usuarioId) {
        ViajeResumen[] viajes = restClient.get()
                .uri("/v1/viajes")
                .header("X-Usuario-Id", usuarioId)
                .retrieve()
                .body(ViajeResumen[].class);
        return viajes == null ? List.of() : List.of(viajes);
    }
}
