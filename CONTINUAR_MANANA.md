# Continuidad De Desarrollo POS

Fecha de corte: 2026-05-13

Este documento resume el estado actual del sistema y deja una ruta clara para continuar el desarrollo en la siguiente sesion.

## Estado Actual

El proyecto ya tiene un nucleo backend POS en Spring Boot con JPA. La app de escritorio JavaFX esta declarada en `pom.xml`, pero todavia no existe implementacion de `com.saas_tienda.desktop.DesktopApp`.

El backend compila y el contexto Spring arranca con H2 en pruebas.

Comando usado para verificar:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn test
```

Nota: el `JAVA_HOME` global de la sesion no estaba bien configurado; por eso se uso el JDK 25 instalado con `--release 17` desde Maven.

## Modulos Implementados

### Productos

Archivos principales:

- `src/main/java/com/saas_tienda/backend/domain/Producto.java`
- `src/main/java/com/saas_tienda/backend/domain/UnidadVenta.java`
- `src/main/java/com/saas_tienda/backend/service/ProductoService.java`
- `src/main/java/com/saas_tienda/backend/web/ProductoController.java`

Funciones:

- Alta y actualizacion de productos.
- SKU unico.
- Codigo de barras opcional y unico.
- Busqueda manual por texto.
- Busqueda exacta por codigo de barras o SKU.
- Unidad de venta: `PIEZA`, `KILO`, `GRAMO`.
- Producto con o sin control de inventario.

Endpoints:

- `GET /productos`
- `GET /productos?q=texto`
- `GET /productos/codigo/{codigo}`
- `POST /productos`
- `PUT /productos/{id}`

### Ventas POS

Archivos principales:

- `src/main/java/com/saas_tienda/backend/domain/Venta.java`
- `src/main/java/com/saas_tienda/backend/domain/VentaDetalle.java`
- `src/main/java/com/saas_tienda/backend/service/VentaService.java`
- `src/main/java/com/saas_tienda/backend/web/VentaController.java`
- `src/main/java/com/saas_tienda/backend/web/PosController.java`

Funciones:

- Venta por producto registrado usando `productoId` o `codigo`.
- Venta por pieza.
- Venta por peso con cantidades decimales.
- Venta rapida sin producto registrado, usando `nombre`, `cantidad`, `precioUnitario`.
- Descuento automatico de inventario cuando el producto controla stock.
- Movimiento de stock por venta.
- Pago simple por efectivo con `pagoCon`.
- Pagos multiples con `EFECTIVO`, `TARJETA`, `TRANSFERENCIA`, `VALE`.
- Ticket generado como texto.
- Envio a impresora termica con ESC/POS.
- Apertura automatica/manual del cajon.

Endpoints:

- `POST /ventas`
- `POST /pos/ventas`
- `POST /pos/cajon/abrir`

Regla importante:

- `POST /pos/ventas` exige turno de caja abierto.
- `POST /ventas` queda mas flexible para uso administrativo o integraciones.

### Caja

Archivos principales:

- `src/main/java/com/saas_tienda/backend/domain/TurnoCaja.java`
- `src/main/java/com/saas_tienda/backend/domain/MovimientoCaja.java`
- `src/main/java/com/saas_tienda/backend/domain/MetodoPago.java`
- `src/main/java/com/saas_tienda/backend/domain/TipoMovimientoCaja.java`
- `src/main/java/com/saas_tienda/backend/service/CajaService.java`
- `src/main/java/com/saas_tienda/backend/web/CajaController.java`

Funciones:

- Abrir turno con fondo inicial.
- Cerrar turno con efectivo contado.
- Calculo de efectivo esperado.
- Calculo de diferencia: sobrante/faltante.
- Movimientos manuales: `ENTRADA`, `RETIRO`, `EGRESO`, `AJUSTE`.
- Movimientos automaticos por venta.
- Movimientos automaticos por devolucion.

Endpoints:

- `POST /caja/turnos/abrir`
- `POST /caja/turnos/{id}/cerrar`
- `GET /caja/turnos/{id}`
- `GET /caja/turnos/actual?tiendaId=&usuarioId=`
- `GET /caja/turnos?tiendaId=`
- `POST /caja/movimientos`
- `GET /caja/turnos/{id}/movimientos`

### Devoluciones Y Cancelaciones

Archivos principales:

- `src/main/java/com/saas_tienda/backend/domain/EstadoVenta.java`
- `src/main/java/com/saas_tienda/backend/domain/DevolucionVenta.java`
- `src/main/java/com/saas_tienda/backend/domain/DevolucionVentaDetalle.java`
- `src/main/java/com/saas_tienda/backend/service/DevolucionService.java`
- `src/main/java/com/saas_tienda/backend/web/DevolucionController.java`

Funciones:

- Estado de venta: `VIGENTE`, `DEVUELTA_PARCIAL`, `CANCELADA`.
- Cantidad devuelta por renglon.
- Validacion para no devolver mas de lo vendido.
- Cancelacion total del ticket.
- Devolucion parcial por renglon.
- Reintegro de inventario si el producto controla stock.
- Movimiento de stock tipo `DEVOLUCION`.
- Movimiento de caja tipo `DEVOLUCION`.

Endpoints:

- `POST /pos/devoluciones`
- `POST /pos/ventas/{ventaId}/cancelar`
- `GET /pos/ventas/{ventaId}/devoluciones`

## Prueba Agregada

Archivo:

- `src/test/java/com/saas_tienda/backend/SaasTiendaApplicationTests.java`

Objetivo:

- Levantar contexto Spring con H2.
- Validar wiring de servicios, controladores, repositorios y entidades JPA.
- Detectar errores de mapeo antes de arrancar la app manualmente.

## Riesgos Tecnicos Actuales

### 1. Seguridad No Implementada

Existe:

- `Usuario`
- `Rol`
- `AuthDtos`
- `passwordHash`

Falta:

- `AuthController`
- servicio de login
- token bearer real
- filtro de autenticacion
- usuario actual desde token
- permisos por rol

Riesgo:

- Hoy los endpoints aceptan `usuarioId` desde el request. Eso es util para avanzar rapido, pero no es seguro para operacion real.

### 2. Permisos Por Rol No Aplicados

Acciones sensibles que deben protegerse:

- abrir cajon manualmente
- cerrar caja
- retirar efectivo
- registrar egresos
- cancelar ventas
- hacer devoluciones
- ajustar inventario
- cambiar precios
- aplicar descuentos

### 3. Endpoints Documentados Pero No Implementados

En `README.md` aparecen endpoints planeados que todavia no tienen controlador/servicio completo:

- `/auth/login`
- `/tiendas`
- `/inventario`
- `/stock/restock`
- `/stock/ajuste`
- `/stock/movimientos`
- `/egresos`
- `/reportes/*`

Existen algunos DTOs/repositorios para esto, pero falta capa web/servicio.

### 4. App De Escritorio Pendiente

El `pom.xml` declara JavaFX:

- `com.saas_tienda.desktop.DesktopApp`

Pero no existe aun.

La app local sera necesaria para:

- pantalla rapida de venta
- lectura de escaner como teclado
- lectura de bascula por puerto serial/USB/HID
- impresion local confiable
- apertura de cajon desde la PC de caja
- posible modo offline

### 5. Migraciones De Base De Datos

Actualmente:

```yaml
spring.jpa.hibernate.ddl-auto: update
```

Para produccion conviene usar Flyway o Liquibase antes de tener datos reales.

## Siguiente Etapa Recomendada

La siguiente etapa debe ser:

## Etapa 1: Seguridad, Login Y Permisos

Objetivo:

Convertir el backend de prototipo funcional a sistema operable por usuarios reales.

Tareas:

1. Agregar dependencia de Spring Security Web si hace falta.
2. Crear `AuthService`.
3. Crear `AuthController` con `POST /auth/login`.
4. Generar token bearer simple y persistible/verificable.
5. Crear filtro de autenticacion.
6. Crear `UsuarioActual` o mecanismo equivalente.
7. Dejar de recibir `usuarioId` en operaciones sensibles.
8. Aplicar permisos por rol.

Roles sugeridos:

- `ADMIN_GLOBAL`: todo, multi-tienda.
- `ENCARGADO_TIENDA`: caja, devoluciones, cancelaciones, inventario, egresos.
- `CAJERO`: venta, consulta de productos, apertura/cierre de su turno.

Cambio recomendado:

Actualizar `Rol.java` para incluir:

```java
ADMIN_GLOBAL,
ENCARGADO_TIENDA,
CAJERO
```

Reglas sugeridas:

- `CAJERO`
  - vender
  - consultar productos
  - abrir/cerrar su turno
  - consultar su caja

- `ENCARGADO_TIENDA`
  - todo lo de cajero
  - devoluciones
  - cancelaciones
  - retiros
  - egresos
  - ajustes de inventario
  - cambio de precios

- `ADMIN_GLOBAL`
  - todo
  - ver todas las tiendas
  - reportes consolidados

## Etapa 2: Stock, Egresos Y Reportes Reales

Despues de seguridad:

1. Implementar `StockService` y `StockController`.
2. Implementar `EgresoService` y `EgresoController`.
3. Integrar egresos con caja.
4. Implementar reportes:
   - ventas por dia
   - ventas por cajero
   - corte de caja
   - utilidad bruta/neta
   - movimientos de inventario
   - productos mas vendidos
   - stock bajo

## Etapa 3: Descuentos Y Precios

Implementar:

- descuento por renglon
- descuento por ticket
- precio manual con permiso
- promociones simples
- historial de cambios de precio

## Etapa 4: JavaFX POS

Construir app local:

- pantalla principal de venta
- barra de busqueda/escaneo
- carrito
- panel de pagos
- apertura/cierre de caja
- devoluciones
- impresion/reimpresion
- lectura de bascula
- configuracion de impresora

## Etapa 5: Produccion

Antes de usar con datos reales:

- Flyway/Liquibase
- perfiles `dev`, `test`, `prod`
- seed controlado de usuarios iniciales
- backups de PostgreSQL
- logs de auditoria
- empaquetado de JavaFX

## Comandos Utiles Para Retomar

Ver archivos:

```powershell
rg --files
```

Buscar controladores:

```powershell
rg -n "@RestController|@RequestMapping" src\main\java
```

Buscar pendientes:

```powershell
rg -n "auth|login|Rol|Security|TODO|ddl-auto|DesktopApp" src README.md pom.xml
```

Compilar y probar:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn test
```

## Recomendacion Para La Siguiente Sesion

Empezar con seguridad. No conviene seguir sumando funciones sensibles hasta que:

- el usuario venga del token
- el rol venga del usuario autenticado
- cada endpoint sensible tenga permiso claro

Primer objetivo de la siguiente sesion:

Implementar `POST /auth/login`, filtro bearer y reemplazar `usuarioId` manual en ventas/caja/devoluciones por usuario autenticado.
