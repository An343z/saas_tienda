package com.saas_tienda.backend.service;

import com.saas_tienda.backend.domain.DevolucionVenta;
import com.saas_tienda.backend.domain.DevolucionVentaDetalle;
import com.saas_tienda.backend.domain.InventarioTienda;
import com.saas_tienda.backend.domain.MetodoPago;
import com.saas_tienda.backend.domain.MovimientoStock;
import com.saas_tienda.backend.domain.Producto;
import com.saas_tienda.backend.domain.TipoMovimientoStock;
import com.saas_tienda.backend.domain.TurnoCaja;
import com.saas_tienda.backend.domain.UnidadVenta;
import com.saas_tienda.backend.domain.Usuario;
import com.saas_tienda.backend.domain.Venta;
import com.saas_tienda.backend.domain.VentaDetalle;
import com.saas_tienda.backend.dto.DevolucionDtos;
import com.saas_tienda.backend.repository.DevolucionVentaRepository;
import com.saas_tienda.backend.repository.InventarioTiendaRepository;
import com.saas_tienda.backend.repository.MovimientoStockRepository;
import com.saas_tienda.backend.repository.UsuarioRepository;
import com.saas_tienda.backend.repository.VentaRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DevolucionService {
    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DevolucionVentaRepository devolucionRepository;
    private final InventarioTiendaRepository inventarioRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final CajaService cajaService;

    public DevolucionService(
            VentaRepository ventaRepository,
            UsuarioRepository usuarioRepository,
            DevolucionVentaRepository devolucionRepository,
            InventarioTiendaRepository inventarioRepository,
            MovimientoStockRepository movimientoStockRepository,
            CajaService cajaService) {
        this.ventaRepository = ventaRepository;
        this.usuarioRepository = usuarioRepository;
        this.devolucionRepository = devolucionRepository;
        this.inventarioRepository = inventarioRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.cajaService = cajaService;
    }

    @Transactional
    public DevolucionVenta cancelar(Long ventaId, DevolucionDtos.CancelarVentaRequest request) {
        return devolver(new DevolucionDtos.DevolucionRequest(
                ventaId,
                request.usuarioId(),
                request.turnoCajaId(),
                request.metodoPagoReembolso(),
                request.motivo(),
                request.referencia(),
                null));
    }

    @Transactional
    public DevolucionVenta devolver(DevolucionDtos.DevolucionRequest request) {
        Venta venta = ventaRepository.findLockedById(request.ventaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada"));
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        TurnoCaja turnoCaja = cajaService.resolverTurnoParaVenta(venta.getTienda(), usuario, request.turnoCajaId(), true);
        MetodoPago metodoPago = request.metodoPagoReembolso() == null ? MetodoPago.EFECTIVO : request.metodoPagoReembolso();
        DevolucionVenta devolucion = new DevolucionVenta(
                venta,
                usuario,
                turnoCaja,
                metodoPago,
                request.motivo().trim(),
                request.referencia());

        List<DevolucionItem> items = resolverItems(venta, request.items());
        if (items.isEmpty()) {
            throw new IllegalArgumentException("La venta no tiene articulos pendientes por devolver");
        }
        for (DevolucionItem item : items) {
            VentaDetalle detalle = item.detalle();
            BigDecimal cantidad = item.cantidad();
            validarCantidad(detalle, cantidad);
            detalle.registrarDevolucion(cantidad);
            devolucion.agregarDetalle(new DevolucionVentaDetalle(detalle, cantidad));
            devolverInventario(venta, usuario, detalle, cantidad);
        }
        venta.actualizarEstadoPorDevoluciones();
        DevolucionVenta guardada = devolucionRepository.save(devolucion);
        cajaService.registrarDevolucion(guardada);
        return guardada;
    }

    @Transactional(readOnly = true)
    public List<DevolucionVenta> listarPorVenta(Long ventaId) {
        return devolucionRepository.findByVentaIdOrderByFechaDesc(ventaId);
    }

    public DevolucionDtos.DevolucionResponse toResponse(DevolucionVenta devolucion) {
        return new DevolucionDtos.DevolucionResponse(
                devolucion.getId(),
                devolucion.getVenta().getId(),
                devolucion.getVenta().getEstado(),
                devolucion.getTienda().getId(),
                devolucion.getTienda().getNombre(),
                devolucion.getUsuario().getId(),
                devolucion.getUsuario().getNombre(),
                devolucion.getTurnoCaja() == null ? null : devolucion.getTurnoCaja().getId(),
                devolucion.getMetodoPagoReembolso(),
                devolucion.getTotal(),
                devolucion.getCostoTotal(),
                devolucion.getUtilidadRevertida(),
                devolucion.getFecha(),
                devolucion.getMotivo(),
                devolucion.getReferencia(),
                devolucion.getDetalles().stream().map(this::toResponse).toList());
    }

    private DevolucionDtos.DevolucionDetalleResponse toResponse(DevolucionVentaDetalle detalle) {
        Producto producto = detalle.getProducto();
        return new DevolucionDtos.DevolucionDetalleResponse(
                detalle.getVentaDetalle().getId(),
                producto == null ? null : producto.getId(),
                detalle.getNombreConcepto(),
                UnidadVenta.valueOf(detalle.getUnidadVenta()),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal());
    }

    private List<DevolucionItem> resolverItems(Venta venta, List<DevolucionDtos.DevolucionItemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return venta.getDetalles().stream()
                    .filter(detalle -> detalle.getCantidadDisponibleDevolucion().compareTo(BigDecimal.ZERO) > 0)
                    .map(detalle -> new DevolucionItem(detalle, detalle.getCantidadDisponibleDevolucion()))
                    .toList();
        }
        return requests.stream()
                .map(request -> new DevolucionItem(buscarDetalle(venta, request.ventaDetalleId()), request.cantidad().stripTrailingZeros()))
                .toList();
    }

    private VentaDetalle buscarDetalle(Venta venta, Long ventaDetalleId) {
        return venta.getDetalles().stream()
                .filter(detalle -> detalle.getId().equals(ventaDetalleId))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Detalle de venta no encontrado en la venta " + venta.getId()));
    }

    private void validarCantidad(VentaDetalle detalle, BigDecimal cantidad) {
        if (cantidad == null || cantidad.compareTo(new BigDecimal("0.001")) < 0) {
            throw new IllegalArgumentException("La cantidad a devolver debe ser mayor a cero");
        }
        if (UnidadVenta.valueOf(detalle.getUnidadVenta()) == UnidadVenta.PIEZA && cantidad.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("La devolucion por pieza no acepta decimales");
        }
    }

    private void devolverInventario(Venta venta, Usuario usuario, VentaDetalle detalle, BigDecimal cantidad) {
        Producto producto = detalle.getProducto();
        if (producto == null || !producto.isControlaInventario()) {
            return;
        }
        InventarioTienda inventario = inventarioRepository.findLockedByTiendaIdAndProductoId(venta.getTienda().getId(), producto.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay inventario para devolver " + producto.getNombre()));
        inventario.sumar(cantidad);
        movimientoStockRepository.save(new MovimientoStock(
                venta.getTienda(),
                producto,
                usuario,
                TipoMovimientoStock.DEVOLUCION,
                cantidad,
                inventario.getExistencia(),
                "Devolucion venta " + venta.getId()));
    }

    private record DevolucionItem(VentaDetalle detalle, BigDecimal cantidad) {
    }
}
