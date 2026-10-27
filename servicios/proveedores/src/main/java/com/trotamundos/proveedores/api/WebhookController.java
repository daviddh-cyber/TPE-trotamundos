package com.trotamundos.proveedores.api;

import com.trotamundos.proveedores.api.generado.WebhooksApi;
import com.trotamundos.proveedores.servicio.MonitoreoService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recibe las notificaciones de los proveedores (RF-09). Implementa la interfaz generada
 * desde specs/proveedores-api.yaml. Responde 202 apenas publica el evento, sin esperar a Viajes.
 */
@RestController
public class WebhookController implements WebhooksApi {

    private final MonitoreoService monitoreoService;

    public WebhookController(MonitoreoService monitoreoService) {
        this.monitoreoService = monitoreoService;
    }

    @Override
    public ResponseEntity<Void> recibirNotificacion(String proveedor, String xClaveProveedor,
                                                    Map<String, Object> notificacion) {
        monitoreoService.procesarNotificacion(proveedor, xClaveProveedor, notificacion);
        return ResponseEntity.accepted().build();
    }
}
