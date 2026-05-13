package com.saas_tienda.backend.dto;

import com.saas_tienda.backend.domain.EstadoVenta;
import com.saas_tienda.backend.domain.MetodoPago;
import com.saas_tienda.backend.domain.UnidadVenta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class VentaDtos {
    private VentaDtos() {
    }

    public record VentaRequest(Long tiendaId, Long usuarioId, Long turnoCajaId, @NotEmpty List<@Valid VentaItemRequest> items, List<@Valid PagoRequest> pagos, BigDecimal pagoCon, Boolean imprimirTicket, Boolean abrirCajon) {
    }

    public record VentaItemRequest(
            Long productoId,
            String codigo,
            @DecimalMin(value = "0.001") BigDecimal cantidad,
            BigDecimal precioUnitario,
            BigDecimal costoUnitario,
            String nombre,
            UnidadVenta unidadVenta) {
    }

    public record PagoRequest(
            @NotNull MetodoPago metodoPago,
            @NotNull @DecimalMin(value = "0.01") BigDecimal monto,
            BigDecimal recibido,
            String referencia) {
    }

    public record VentaResponse(Long id, Long tiendaId, String tiendaNombre, Long turnoCajaId, Instant fecha, EstadoVenta estado, BigDecimal total, BigDecimal costoTotal, BigDecimal utilidad, BigDecimal pagoCon, BigDecimal cambio, List<PagoResponse> pagos, List<VentaDetalleResponse> detalles, TicketResponse ticket) {
    }

    public record VentaDetalleResponse(Long id, Long productoId, String sku, String codigoBarras, String productoNombre, UnidadVenta unidadVenta, boolean ventaRapida, BigDecimal cantidad, BigDecimal cantidadDevuelta, BigDecimal cantidadDisponibleDevolucion, BigDecimal costoUnitario, BigDecimal precioUnitario, BigDecimal subtotal, BigDecimal utilidad) {
    }

    public record PagoResponse(MetodoPago metodoPago, BigDecimal monto, BigDecimal recibido, BigDecimal cambio, String referencia) {
    }

    public record TicketResponse(String folio, String contenido, boolean impreso, boolean cajonAbierto, String impresora, String mensajeImpresion) {
    }
}
