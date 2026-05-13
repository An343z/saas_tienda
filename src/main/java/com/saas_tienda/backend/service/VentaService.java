package com.saas_tienda.backend.service;

import com.saas_tienda.backend.domain.InventarioTienda;
import com.saas_tienda.backend.domain.MetodoPago;
import com.saas_tienda.backend.domain.MovimientoStock;
import com.saas_tienda.backend.domain.PagoVenta;
import com.saas_tienda.backend.domain.Producto;
import com.saas_tienda.backend.domain.Tienda;
import com.saas_tienda.backend.domain.TipoMovimientoStock;
import com.saas_tienda.backend.domain.TurnoCaja;
import com.saas_tienda.backend.domain.UnidadVenta;
import com.saas_tienda.backend.domain.Usuario;
import com.saas_tienda.backend.domain.Venta;
import com.saas_tienda.backend.domain.VentaDetalle;
import com.saas_tienda.backend.dto.VentaDtos;
import com.saas_tienda.backend.repository.InventarioTiendaRepository;
import com.saas_tienda.backend.repository.MovimientoStockRepository;
import com.saas_tienda.backend.repository.ProductoRepository;
import com.saas_tienda.backend.repository.TiendaRepository;
import com.saas_tienda.backend.repository.UsuarioRepository;
import com.saas_tienda.backend.repository.VentaRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VentaService {
    private final VentaRepository ventaRepository;
    private final TiendaRepository tiendaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final InventarioTiendaRepository inventarioRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final TicketService ticketService;
    private final ImpresionTicketService impresionTicketService;
    private final CajaService cajaService;

    public VentaService(
            VentaRepository ventaRepository,
            TiendaRepository tiendaRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            InventarioTiendaRepository inventarioRepository,
            MovimientoStockRepository movimientoStockRepository,
            TicketService ticketService,
            ImpresionTicketService impresionTicketService,
            CajaService cajaService) {
        this.ventaRepository = ventaRepository;
        this.tiendaRepository = tiendaRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.ticketService = ticketService;
        this.impresionTicketService = impresionTicketService;
        this.cajaService = cajaService;
    }

    @Transactional
    public VentaResultado registrar(VentaDtos.VentaRequest request) {
        return registrar(request, false);
    }

    @Transactional
    public VentaResultado registrarPos(VentaDtos.VentaRequest request) {
        return registrar(request, true);
    }

    private VentaResultado registrar(VentaDtos.VentaRequest request, boolean requiereTurnoCaja) {
        if (request.tiendaId() == null) {
            throw new IllegalArgumentException("La tienda es obligatoria");
        }
        if (request.usuarioId() == null) {
            throw new IllegalArgumentException("El usuario/cajero es obligatorio");
        }
        Tienda tienda = tiendaRepository.findById(request.tiendaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Tienda no encontrada"));
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        Venta venta = new Venta(tienda, usuario);
        TurnoCaja turnoCaja = cajaService.resolverTurnoParaVenta(tienda, usuario, request.turnoCajaId(), requiereTurnoCaja);
        venta.setTurnoCaja(turnoCaja);

        for (VentaDtos.VentaItemRequest item : request.items()) {
            Producto producto = resolverProducto(item);
            BigDecimal cantidad = cantidad(item, producto);
            if (producto == null) {
                venta.agregarDetalle(crearDetalleVentaRapida(item, cantidad));
                continue;
            }
            validarCantidadProducto(producto, cantidad);
            if (producto.isControlaInventario()) {
                InventarioTienda inventario = inventarioRepository.findLockedByTiendaIdAndProductoId(tienda.getId(), producto.getId())
                        .orElseThrow(() -> new RecursoNoEncontradoException("No hay inventario para " + producto.getNombre()));
                inventario.restar(cantidad);
                movimientoStockRepository.save(new MovimientoStock(
                        tienda,
                        producto,
                        usuario,
                        TipoMovimientoStock.VENTA,
                        cantidad,
                        inventario.getExistencia(),
                        "Venta en punto de venta"));
            }
            venta.agregarDetalle(new VentaDetalle(producto, cantidad, producto.getCostoBase(), producto.getPrecioBase()));
        }

        List<PagoVenta> pagos = resolverPagos(request, venta.getTotal());
        pagos.forEach(venta::agregarPago);
        Venta guardada = ventaRepository.save(venta);
        cajaService.registrarVenta(guardada);
        BigDecimal pagoCon = pagoCon(guardada);
        BigDecimal cambio = guardada.getCambio();
        String contenidoTicket = ticketService.crearTicket(guardada, pagoCon, cambio);
        ImpresionTicketService.ResultadoImpresion impresion = ejecutarImpresion(request, contenidoTicket);
        VentaDtos.TicketResponse ticket = new VentaDtos.TicketResponse(
                ticketService.folio(guardada),
                contenidoTicket,
                impresion.impreso(),
                impresion.cajonAbierto(),
                impresion.impresora(),
                impresion.mensaje());
        return new VentaResultado(guardada, pagoCon, cambio, ticket);
    }

    public ImpresionTicketService.ResultadoImpresion abrirCajon() {
        return impresionTicketService.abrirCajon();
    }

    public VentaDtos.VentaResponse toResponse(VentaResultado resultado) {
        Venta venta = resultado.venta();
        List<VentaDtos.PagoResponse> pagos = venta.getPagos().stream()
                .map(pago -> new VentaDtos.PagoResponse(
                        pago.getMetodoPago(),
                        pago.getMonto(),
                        pago.getRecibido(),
                        pago.getCambio(),
                        pago.getReferencia()))
                .toList();
        List<VentaDtos.VentaDetalleResponse> detalles = venta.getDetalles().stream()
                .map(detalle -> {
                    Producto producto = detalle.getProducto();
                    return new VentaDtos.VentaDetalleResponse(
                            detalle.getId(),
                            producto == null ? null : producto.getId(),
                            producto == null ? null : producto.getSku(),
                            producto == null ? null : producto.getCodigoBarras(),
                            detalle.getNombreConcepto(),
                            UnidadVenta.valueOf(detalle.getUnidadVenta()),
                            detalle.isVentaRapida(),
                            detalle.getCantidad(),
                            detalle.getCantidadDevuelta(),
                            detalle.getCantidadDisponibleDevolucion(),
                            detalle.getCostoUnitario(),
                            detalle.getPrecioUnitario(),
                            detalle.getSubtotal(),
                            detalle.getUtilidad());
                })
                .toList();
        return new VentaDtos.VentaResponse(
                venta.getId(),
                venta.getTienda().getId(),
                venta.getTienda().getNombre(),
                venta.getTurnoCaja() == null ? null : venta.getTurnoCaja().getId(),
                venta.getFecha(),
                venta.getEstado(),
                venta.getTotal(),
                venta.getCostoTotal(),
                venta.getUtilidad(),
                resultado.pagoCon(),
                resultado.cambio(),
                pagos,
                detalles,
                resultado.ticket());
    }

    private Producto resolverProducto(VentaDtos.VentaItemRequest item) {
        if (item.productoId() != null) {
            return productoRepository.findById(item.productoId())
                    .filter(Producto::isActivo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado o inactivo"));
        }
        if (item.codigo() != null && !item.codigo().isBlank()) {
            return productoRepository.findActivoByCodigoExacto(item.codigo().trim())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado para codigo " + item.codigo()));
        }
        if (item.nombre() != null && !item.nombre().isBlank()) {
            return null;
        }
        throw new IllegalArgumentException("Cada articulo debe incluir productoId, codigo o nombre para venta rapida");
    }

    private BigDecimal cantidad(VentaDtos.VentaItemRequest item, Producto producto) {
        BigDecimal cantidad = item.cantidad();
        if (cantidad == null && producto != null && producto.getUnidadVenta() == UnidadVenta.PIEZA) {
            cantidad = BigDecimal.ONE;
        }
        if (cantidad == null || cantidad.compareTo(new BigDecimal("0.001")) < 0) {
            throw new IllegalArgumentException("La cantidad/peso debe ser mayor a cero");
        }
        return cantidad.stripTrailingZeros();
    }

    private void validarCantidadProducto(Producto producto, BigDecimal cantidad) {
        if (!producto.aceptaCantidadDecimal() && cantidad.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("El producto " + producto.getNombre() + " se vende por pieza y no acepta decimales");
        }
    }

    private VentaDetalle crearDetalleVentaRapida(VentaDtos.VentaItemRequest item, BigDecimal cantidad) {
        BigDecimal precioUnitario = item.precioUnitario();
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La venta rapida requiere precioUnitario mayor a cero");
        }
        BigDecimal costoUnitario = item.costoUnitario() == null ? BigDecimal.ZERO : item.costoUnitario();
        if (costoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costoUnitario no puede ser negativo");
        }
        UnidadVenta unidadVenta = item.unidadVenta() == null ? UnidadVenta.KILO : item.unidadVenta();
        if (unidadVenta == UnidadVenta.PIEZA && cantidad.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("La venta rapida por pieza no acepta decimales");
        }
        return new VentaDetalle(item.nombre().trim(), unidadVenta.name(), cantidad, costoUnitario, precioUnitario);
    }

    private List<PagoVenta> resolverPagos(VentaDtos.VentaRequest request, BigDecimal total) {
        if (request.pagos() != null && !request.pagos().isEmpty()) {
            List<PagoVenta> pagos = request.pagos().stream().map(this::crearPago).toList();
            BigDecimal pagado = pagos.stream().map(PagoVenta::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (pagado.compareTo(total) != 0) {
                throw new IllegalArgumentException("La suma de pagos debe ser igual al total de la venta");
            }
            return pagos;
        }
        BigDecimal recibido = request.pagoCon() == null ? total : request.pagoCon();
        if (recibido.compareTo(total) < 0) {
            throw new IllegalArgumentException("El pago no cubre el total de la venta");
        }
        return List.of(new PagoVenta(MetodoPago.EFECTIVO, total, recibido, recibido.subtract(total), null));
    }

    private PagoVenta crearPago(VentaDtos.PagoRequest request) {
        BigDecimal monto = request.monto();
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cada pago debe tener monto mayor a cero");
        }
        MetodoPago metodoPago = request.metodoPago();
        BigDecimal recibido = request.recibido() == null ? monto : request.recibido();
        BigDecimal cambio = BigDecimal.ZERO;
        if (metodoPago == MetodoPago.EFECTIVO) {
            if (recibido.compareTo(monto) < 0) {
                throw new IllegalArgumentException("El efectivo recibido no cubre el monto aplicado");
            }
            cambio = recibido.subtract(monto);
        } else {
            recibido = monto;
        }
        return new PagoVenta(metodoPago, monto, recibido, cambio, request.referencia());
    }

    private BigDecimal pagoCon(Venta venta) {
        return venta.getPagos().stream()
                .filter(pago -> pago.getMetodoPago() == MetodoPago.EFECTIVO)
                .map(PagoVenta::getRecibido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ImpresionTicketService.ResultadoImpresion ejecutarImpresion(VentaDtos.VentaRequest request, String contenidoTicket) {
        boolean imprimirTicket = Boolean.TRUE.equals(request.imprimirTicket());
        boolean abrirCajon = request.abrirCajon() == null || Boolean.TRUE.equals(request.abrirCajon());
        if (imprimirTicket) {
            return impresionTicketService.imprimir(contenidoTicket, abrirCajon);
        }
        if (abrirCajon) {
            return impresionTicketService.abrirCajon();
        }
        return new ImpresionTicketService.ResultadoImpresion(false, false, null, "Ticket generado sin impresion");
    }

    public record VentaResultado(Venta venta, BigDecimal pagoCon, BigDecimal cambio, VentaDtos.TicketResponse ticket) {
    }
}
