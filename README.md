# Trotamundos – Prototipo

Trabajo Práctico Especial de **Diseño de Sistemas de Software** (UNICEN).
Integrantes: David Herrera, Carlos Urquiza.

La documentación de la arquitectura (análisis, drivers, iteraciones de ADD y ADRs) está en la
[Wiki del repositorio](https://github.com/daviddh-cyber/TPE-trotamundos/wiki).

## Qué demuestra el prototipo

Implementa el recorrido del **diagrama de secuencia de la Iteración 2 (sección 6.3)**:
un proveedor de viaje avisa un cambio en una reserva, y el cambio llega al panel del viajero.

1. El viajero carga una reserva en **Viajes**, que la agrupa en un viaje y publica `ReservaRegistrada`.
2. **Proveedores** consume ese evento y empieza a monitorear la reserva.
3. Una aerolínea simulada envía un webhook con un cambio de horario, en su propio formato.
4. El adaptador de la aerolínea lo traduce al modelo común y Proveedores publica `ReservaActualizada`.
5. **Viajes** consume el evento (descartando repetidos), actualiza la reserva y publica `ViajeActualizado`.
6. El panel (`GET /v1/viajes`) muestra la reserva actualizada.

### Asistente virtual (2da entrega)

Implementa el servicio **Asistente** de las Iteraciones 5 y 6 de la wiki, **sin modificar Viajes ni Proveedores**:
el asistente usa la misma API pública que la app del viajero (IA-7, ADR 014).

1. **Camino rápido** (ADR 017): las consultas de estado ("¿cómo está mi vuelo?") se responden con una plantilla
   armada con los datos de Viajes, sin consultar al modelo de lenguaje.
2. **El modelo propone, el orquestador decide** (ADR 015): el resto de los mensajes los interpreta el modelo,
   que solo propone herramientas. El orquestador valida cada propuesta antes de ejecutarla.
3. **Confirmación** (RF-28, SE-5): derivar el caso a la agencia queda pendiente hasta que el usuario lo confirma
   con otra petición. Recién ahí se publica el evento `CasoDerivado` en Kafka.
4. **Defensa en profundidad** (ADR 019): al modelo no le llegan los códigos de reserva ni datos sensibles;
   una propuesta con un usuario ajeno se rechaza; cada intento de manipulación se audita, y con 3 intentos
   en 1 hora el asistente se bloquea para ese usuario.

## Trazabilidad con la arquitectura

| Elemento del prototipo | Decisión o requerimiento |
|------------------------|--------------------------|
| Servicios `viajes` y `proveedores`, cada uno con su base PostgreSQL | ADR 001 (microservicios, una base por servicio) |
| REST para consultas + eventos en Kafka entre servicios | ADR 002 (comunicación híbrida) |
| Kafka con clave `proveedor:codigo` | ADR 004 (orden por reserva) |
| Java 21 + Spring Boot 3 | ADR 005 |
| `Reserva` con núcleo común + `detalles` en JSONB | ADR 006 (modelo extensible, EV-1) |
| Capas `api` → `servicio` → `dominio` → `datos`; `integracion/AdaptadorProveedor` | ADR 007 (capas + patrón Adaptador, IN-1) |
| `specs/*.yaml` y controladores que implementan interfaces generadas | R-3 (Spec-driven) |
| `EventoProcesado` (Viajes) y control por `reservaId` (Proveedores) | Idempotencia (Iteración 2, sección 5.6) |
| `AgrupacionService` | RF-13, supuesto S-14 |
| Clave por proveedor en los webhooks | Riesgo RI-6 |
| Servicio `asistente`, que llama a Viajes por su API pública | ADR 014 (IA-7: sin cambios en Viajes ni Proveedores) |
| `Orquestador` + `RegistroHerramientas` + validación de las propuestas | ADR 015 (el modelo propone, el orquestador decide) |
| Interfaz `ModeloLenguaje` con la implementación `MotorReglas` | ADR 016 (Bedrock detrás de un adaptador) |
| `MotorReglas.esConsultaDeEstado` + `PlantillasRespuesta` | ADR 017 (camino rápido sin LLM) |
| `PoliticaConfirmacion`, `FiltroDatosSensibles`, `DetectorManipulacion`, `AuditoriaSeguridad` | ADR 019 (defensa en profundidad) |
| Evento `CasoDerivado` en `asistente.casos-derivados` | RF-27, US-4 (Iteración 5, decisión 4.5) |

## Estructura

```
specs/                       Contratos (se escriben antes que el código)
  viajes-api.yaml            OpenAPI del servicio Viajes
  proveedores-api.yaml       OpenAPI del servicio Proveedores
  asistente-api.yaml         OpenAPI del servicio Asistente
  eventos-asyncapi.yaml      AsyncAPI de los eventos en Kafka
servicios/
  viajes/                    Servicio Viajes (puerto 8081)
  proveedores/               Servicio Proveedores (puerto 8082)
  asistente/                 Servicio Asistente (puerto 8083)
scripts/demo.sh              Demostración del recorrido de actualización de reservas
scripts/demo-asistente.sh    Demostración del asistente virtual
docker-compose.yml           Kafka + 2 PostgreSQL + los 3 servicios
```

Cada servicio genera en el build las interfaces de sus controladores a partir de su spec OpenAPI
(`target/generated-sources`). Si el código no respeta la spec, no compila.

## Cómo ejecutarlo

Requisitos: Docker con Docker Compose. Java y Maven no son necesarios: el build se hace dentro de Docker.

```bash
docker compose up --build
```

La primera vez tarda varios minutos (descarga imágenes y dependencias). El sistema está listo cuando
en los logs aparecen `Started ViajesApplication`, `Started ProveedoresApplication` y `Started AsistenteApplication`.

En otra terminal:

```bash
./scripts/demo.sh
./scripts/demo-asistente.sh
```

Para apagarlo: `Ctrl + C` y después `docker compose down` (agregar `-v` para borrar también los datos).

## Simplificaciones del prototipo

El prototipo acota el alcance para cumplir el plazo (restricción R-4):

- **Sin API Gateway ni proveedor de identidad** (ADR 003): el usuario se identifica con el header `X-Usuario-Id`, que en producción completaría el gateway a partir del token.
- **Solo Viajes, Proveedores y Asistente**: Correo, Cuentas, Compartir, Comunicaciones y Analítica no se implementan.
- **Proveedores simulados**: `aerolinea-demo` y `hotel-demo`, con formatos distintos para mostrar el patrón Adaptador.
- **Un solo nodo de Kafka y de cada base**: la replicación y el despliegue en varias regiones se diseñan en la Iteración 3, pero no se levantan localmente.
- **Asistente sin LLM real** (deuda técnica DT-4): la única implementación de `ModeloLenguaje` es el `MotorReglas`, que reconoce los mensajes con palabras clave. Para demostrar la seguridad, simula un modelo engañado: si un mensaje nombra a otro usuario, propone consultar sus datos, y el orquestador lo rechaza.
- **Asistente sin respuesta por partes ni conversaciones persistentes**: la respuesta se envía completa (en producción, por Server-Sent Events) y las conversaciones se guardan en memoria (en producción, en PostgreSQL).
- **Sin reprogramaciones**: el prototipo del asistente implementa las herramientas `consultarViajes` y `derivarCaso`; la reprogramación (RF-25) requiere las operaciones nuevas de Proveedores, que se diseñan pero no se implementan.
