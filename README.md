# SaaS Tienda

Sistema Java para gestionar varias tiendas con una sola base de datos PostgreSQL, API Spring Boot en la nube y app de escritorio JavaFX.

## Componentes

- Backend REST: Spring Boot, JPA, PostgreSQL, autenticacion por token bearer.
- Escritorio: JavaFX conectado exclusivamente a la API.
- Reportes: ventas, ingresos, egresos, stock y utilidad, con exportacion PDF y Excel.
- Despliegue: Docker para API y PostgreSQL.

## Ejecucion local

Requisitos: Java 17+, Maven 3.9+ y Docker opcional.

```powershell
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

Usuarios iniciales:

- Admin global: `admin` / `admin123`
- Tienda centro: `centro` / `tienda123`
- Tienda norte: `norte` / `tienda123`

Para levantar PostgreSQL y la API con Docker:

```powershell
docker compose up --build
```

Para abrir la app de escritorio:

```powershell
mvn javafx:run
```

Si la API esta en otra URL:

```powershell
$env:SAAS_TIENDA_API_URL="https://api.tudominio.com"; mvn javafx:run
```

## Endpoints principales

- `POST /auth/login`
- `GET /tiendas`
- `GET /productos`
- `GET /productos?q=` busqueda manual por nombre, SKU o codigo de barras
- `GET /productos/codigo/{codigo}` lectura exacta por SKU o codigo de barras
- `POST /productos`
- `PUT /productos/{id}`
- `GET /inventario?tiendaId=`
- `POST /ventas`
- `POST /pos/ventas` venta de punto de venta con ticket, impresion termica y apertura de cajon
- `POST /pos/devoluciones` devolucion parcial con reversa de caja e inventario
- `POST /pos/ventas/{ventaId}/cancelar` cancelacion/devolucion total del ticket
- `GET /pos/ventas/{ventaId}/devoluciones`
- `POST /pos/cajon/abrir` apertura manual del cajon por pulso a la impresora
- `POST /caja/turnos/abrir`
- `POST /caja/turnos/{id}/cerrar`
- `GET /caja/turnos/actual?tiendaId=&usuarioId=`
- `GET /caja/turnos/{id}/movimientos`
- `POST /caja/movimientos`
- `POST /stock/restock`
- `POST /stock/ajuste`
- `GET /stock/movimientos`
- `GET /egresos`
- `POST /egresos`
- `GET /reportes/ventas`
- `GET /reportes/stock`
- `GET /reportes/utilidad`
- `GET /reportes/export/pdf`
- `GET /reportes/export/excel`

## Seguridad multi-tienda

El backend aplica el filtro por tienda. Un usuario `USUARIO_TIENDA` solo puede operar su `tienda_id`; si manda otro `tiendaId`, la API lo rechaza o lo reemplaza por la tienda asignada segun el endpoint. El admin global puede consultar consolidado o filtrar por tienda.

## Punto de venta

Antes de vender desde `POST /pos/ventas`, el cajero debe tener turno de caja abierto:

```json
{
  "tiendaId": 1,
  "usuarioId": 2,
  "fondoInicial": 500.00,
  "observaciones": "Apertura turno matutino"
}
```

Los lectores de codigo de barras que funcionan como teclado pueden enviar el codigo al campo de busqueda. Usa `GET /productos/codigo/{codigo}` para lectura exacta de escaner, o `GET /productos?q=texto` para busqueda manual.

Ejemplo de venta:

```json
{
  "tiendaId": 1,
  "usuarioId": 2,
  "pagoCon": 200.00,
  "imprimirTicket": true,
  "abrirCajon": true,
  "items": [
    { "codigo": "7501234567890", "cantidad": 1 },
    { "productoId": 5, "cantidad": 2 }
  ]
}
```

Tambien se aceptan pagos multiples:

```json
{
  "tiendaId": 1,
  "usuarioId": 2,
  "items": [
    { "codigo": "7501234567890", "cantidad": 1 }
  ],
  "pagos": [
    { "metodoPago": "EFECTIVO", "monto": 50.00, "recibido": 100.00 },
    { "metodoPago": "TARJETA", "monto": 75.50, "referencia": "AUT-12345" }
  ]
}
```

Cancelacion total de ticket:

```json
{
  "usuarioId": 2,
  "turnoCajaId": 1,
  "metodoPagoReembolso": "EFECTIVO",
  "motivo": "Cliente cancela la compra completa"
}
```

Devolucion parcial:

```json
{
  "ventaId": 10,
  "usuarioId": 2,
  "turnoCajaId": 1,
  "metodoPagoReembolso": "EFECTIVO",
  "motivo": "Producto devuelto por el cliente",
  "items": [
    { "ventaDetalleId": 25, "cantidad": 1 },
    { "ventaDetalleId": 26, "cantidad": 0.250 }
  ]
}
```

La devolucion reintegra inventario cuando el producto controla stock, registra movimiento de caja por reembolso y actualiza la venta a `DEVUELTA_PARCIAL` o `CANCELADA`.

Producto pesable registrado en inventario:

```json
{
  "sku": "TOMATE-KG",
  "codigoBarras": "2000000000017",
  "nombre": "Tomate saladet",
  "costoBase": 18.00,
  "precioBase": 29.90,
  "unidadVenta": "KILO",
  "controlaInventario": true,
  "activo": true
}
```

Venta por peso usando producto registrado:

```json
{
  "tiendaId": 1,
  "usuarioId": 2,
  "imprimirTicket": true,
  "items": [
    { "codigo": "2000000000017", "cantidad": 0.350 }
  ]
}
```

Venta rapida por peso, sin producto en catalogo ni inventario:

```json
{
  "tiendaId": 1,
  "usuarioId": 2,
  "items": [
    {
      "nombre": "Venta rapida bascula",
      "unidadVenta": "KILO",
      "cantidad": 0.725,
      "precioUnitario": 42.00
    }
  ]
}
```

La cantidad puede venir de una balanza conectada a la app de escritorio o capturarse manualmente. El backend recibe el peso en `cantidad`; la lectura fisica de puerto serial/USB debe ocurrir en la PC donde esta conectada la balanza.

Para elegir la impresora termica conectada a la PC donde corre la API:

```powershell
$env:SAAS_TIENDA_IMPRESORA_TERMICA="Nombre exacto de la impresora"; mvn spring-boot:run
```

Si no se configura, el sistema intenta usar la impresora predeterminada. La apertura automatica del cajon se envia con comando ESC/POS a la impresora; la llave manual sigue funcionando de forma fisica e independiente del sistema.
En ventas POS, `abrirCajon` abre automaticamente por default; envia `"abrirCajon": false` solo cuando no quieras emitir el pulso.

Funciones POS cubiertas actualmente:

- Busqueda manual por nombre, SKU o codigo de barras.
- Lectura por escaner de codigo de barras.
- Venta por pieza y por peso.
- Venta rapida con precio y peso sin inventario.
- Descuento automatico de inventario cuando el producto lo controla.
- Registro de movimiento de stock por venta.
- Calculo de total, pago y cambio.
- Apertura y cierre de turno de caja.
- Pagos por efectivo, tarjeta, transferencia o vale.
- Movimientos manuales de caja: entradas, retiros, egresos y ajustes.
- Cancelacion y devolucion parcial con reversa de inventario/caja.
- Generacion de ticket.
- Impresion termica ESC/POS.
- Apertura automatica o manual del cajon.

Funciones recomendadas para cerrar un POS completo en una siguiente iteracion:

- Descuentos por renglon y por ticket.
- Lectura local de bascula en la app de escritorio.
- Reimpresion de ticket y consulta historica.
- Permisos por rol para abrir cajon, cancelar ventas y aplicar descuentos.
- Integracion de egresos con movimientos de caja.
- Reporte de corte impreso por turno.
