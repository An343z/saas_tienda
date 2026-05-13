package com.saas_tienda.backend.dto;

import com.saas_tienda.backend.domain.Egreso;
import com.saas_tienda.backend.domain.InventarioTienda;
import com.saas_tienda.backend.domain.MovimientoStock;
import com.saas_tienda.backend.domain.Producto;
import com.saas_tienda.backend.domain.Tienda;
import com.saas_tienda.backend.domain.UnidadVenta;
import com.saas_tienda.backend.domain.Venta;
import com.saas_tienda.backend.domain.VentaDetalle;
import java.util.List;

public final class DtoMapper {
    private DtoMapper() {
    }

    public static TiendaDto tienda(Tienda tienda) {
        return new TiendaDto(tienda.getId(), tienda.getNombre(), tienda.getDireccion(), tienda.isActiva());
    }

    public static ProductoDtos.ProductoResponse producto(Producto producto) {
        return new ProductoDtos.ProductoResponse(producto.getId(), producto.getSku(), producto.getCodigoBarras(), producto.getNombre(), producto.getCostoBase(), producto.getPrecioBase(), producto.getUnidadVenta(), producto.isControlaInventario(), producto.isActivo());
    }

    public static StockDtos.InventarioResponse inventario(InventarioTienda inventario) {
        Producto producto = inventario.getProducto();
        return new StockDtos.InventarioResponse(
                inventario.getTienda().getId(),
                inventario.getTienda().getNombre(),
                producto.getId(),
                producto.getSku(),
                producto.getNombre(),
                producto.getCostoBase(),
                producto.getPrecioBase(),
                inventario.getExistencia());
    }

    public static StockDtos.MovimientoStockResponse movimiento(MovimientoStock movimiento) {
        return new StockDtos.MovimientoStockResponse(
                movimiento.getId(),
                movimiento.getTienda().getId(),
                movimiento.getTienda().getNombre(),
                movimiento.getProducto().getId(),
                movimiento.getProducto().getNombre(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getExistenciaPosterior(),
                movimiento.getMotivo(),
                movimiento.getFecha());
    }

    public static VentaDtos.VentaResponse venta(Venta venta) {
        List<VentaDtos.VentaDetalleResponse> detalles = venta.getDetalles().stream().map(DtoMapper::ventaDetalle).toList();
        List<VentaDtos.PagoResponse> pagos = venta.getPagos().stream()
                .map(pago -> new VentaDtos.PagoResponse(pago.getMetodoPago(), pago.getMonto(), pago.getRecibido(), pago.getCambio(), pago.getReferencia()))
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
                null,
                venta.getCambio(),
                pagos,
                detalles,
                null);
    }

    private static VentaDtos.VentaDetalleResponse ventaDetalle(VentaDetalle detalle) {
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
    }

    public static EgresoDtos.EgresoResponse egreso(Egreso egreso) {
        return new EgresoDtos.EgresoResponse(egreso.getId(), egreso.getTienda().getId(), egreso.getTienda().getNombre(), egreso.getCategoria(), egreso.getMonto(), egreso.getDescripcion(), egreso.getFecha());
    }
}
