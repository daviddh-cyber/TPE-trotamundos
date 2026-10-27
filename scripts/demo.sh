#!/usr/bin/env bash
# Demostración del recorrido RF-09 / RE-1 (Wiki, Iteración 2, diagrama de secuencia 6.3).
# Requisito: el sistema levantado con "docker compose up --build".
set -euo pipefail

VIAJES=http://localhost:8081
PROVEEDORES=http://localhost:8082
USUARIO=usuario-demo
CODIGO="ABC$RANDOM"

mostrar() { python3 -m json.tool; }

echo "== 1. El viajero carga a mano un vuelo (RF-12). Viajes lo agrupa en un viaje (RF-13)"
curl -s -X POST "$VIAJES/v1/reservas" \
  -H "Content-Type: application/json" -H "X-Usuario-Id: $USUARIO" \
  -d "{
    \"tipo\": \"VUELO\", \"proveedor\": \"aerolinea-demo\", \"codigo\": \"$CODIGO\",
    \"fechaInicio\": \"2026-11-10T08:00:00Z\", \"fechaFin\": \"2026-11-10T10:30:00Z\",
    \"origen\": \"Buenos Aires\", \"destino\": \"Bariloche\",
    \"detalles\": { \"numeroVuelo\": \"AD1234\", \"asiento\": \"14C\" }
  }" | mostrar

echo
echo "Esperando que Proveedores reciba el evento ReservaRegistrada..."
sleep 3

echo "== 2. Proveedores ya monitorea la reserva"
curl -s "$PROVEEDORES/v1/reservas-monitoreadas" | mostrar

echo
echo "== 3. La aerolínea avisa un cambio de horario y de puerta (webhook en SU formato)"
curl -s -o /dev/null -w "Respuesta del webhook: HTTP %{http_code}\n" \
  -X POST "$PROVEEDORES/v1/webhooks/aerolinea-demo" \
  -H "Content-Type: application/json" -H "X-Clave-Proveedor: clave-aerolinea-demo" \
  -d "{
    \"pnr\": \"$CODIGO\", \"evento\": \"SCHEDULE_CHANGE\",
    \"nuevaSalida\": \"2026-11-10T09:30:00Z\", \"nuevaLlegada\": \"2026-11-10T12:00:00Z\",
    \"puerta\": \"B12\"
  }"

echo
echo "Esperando que Viajes procese el evento ReservaActualizada..."
sleep 3

echo "== 4. El panel del viajero ya muestra el nuevo horario y la puerta"
curl -s "$VIAJES/v1/viajes" -H "X-Usuario-Id: $USUARIO" | mostrar

echo
echo "== 5. Un webhook con una clave incorrecta se rechaza (seguridad, RI-6)"
curl -s -o /dev/null -w "Respuesta: HTTP %{http_code} (se espera 401)\n" \
  -X POST "$PROVEEDORES/v1/webhooks/aerolinea-demo" \
  -H "Content-Type: application/json" -H "X-Clave-Proveedor: clave-falsa" \
  -d "{ \"pnr\": \"$CODIGO\", \"evento\": \"CANCELLED\" }"
