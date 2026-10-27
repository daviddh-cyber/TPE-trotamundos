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

## Estructura

```
specs/                       Contratos (se escriben antes que el código)
  viajes-api.yaml            OpenAPI del servicio Viajes
  proveedores-api.yaml       OpenAPI del servicio Proveedores
  eventos-asyncapi.yaml      AsyncAPI de los eventos en Kafka
servicios/
  viajes/                    Servicio Viajes (puerto 8081)
  proveedores/               Servicio Proveedores (puerto 8082)
scripts/demo.sh              Demostración del recorrido completo
docker-compose.yml           Kafka + 2 PostgreSQL + los 2 servicios
```

Cada servicio genera en el build las interfaces de sus controladores a partir de su spec OpenAPI
(`target/generated-sources`). Si el código no respeta la spec, no compila.

## Cómo ejecutarlo

Requisitos: Docker con Docker Compose. Java y Maven no son necesarios: el build se hace dentro de Docker.

```bash
docker compose up --build
```

La primera vez tarda varios minutos (descarga imágenes y dependencias). El sistema está listo cuando
en los logs aparecen `Started ViajesApplication` y `Started ProveedoresApplication`.

En otra terminal:

```bash
./scripts/demo.sh
```

Para apagarlo: `Ctrl + C` y después `docker compose down` (agregar `-v` para borrar también los datos).

## Simplificaciones del prototipo

El prototipo acota el alcance para cumplir el plazo (restricción R-4):

- **Sin API Gateway ni proveedor de identidad** (ADR 003): el usuario se identifica con el header `X-Usuario-Id`, que en producción completaría el gateway a partir del token.
- **Solo Viajes y Proveedores**: Correo, Cuentas, Compartir, Comunicaciones y Analítica no se implementan.
- **Proveedores simulados**: `aerolinea-demo` y `hotel-demo`, con formatos distintos para mostrar el patrón Adaptador.
- **Un solo nodo de Kafka y de cada base**: la replicación y el despliegue en varias regiones se diseñan en la Iteración 3, pero no se levantan localmente.
