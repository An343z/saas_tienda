package com.saas_tienda.backend.dto;

import com.saas_tienda.backend.domain.EstadoVenta;
import com.saas_tienda.backend.domain.MetodoPago;
import com.saas_tienda.backend.domain.UnidadVenta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class DevolucionDtos {
    private DevolucionDtos() {
    }

    public record DevolucionRequest(
            @NotNull Long ventaId,
            @NotNull Long usuarioId,
            Long turnoCajaId,
            MetodoPago metodoPagoReembolso,
            @NotBlank String motivo,
            String referencia,
            List<@Valid DevolucionItemRequest> items) {
    }

    public record CancelarVentaRequest(
            @NotNull Long usuarioId,
            Long turnoCajaId,
            MetodoPago metodoPagoReembolso,
            @NotBlank String motivo,
            String referencia) {
    }

    public record DevolucionItemRequest(
            @NotNull Long ventaDetalleId,
            @NotNull @DecimalMin(value = "0.001") BigDecimal cantidad) {
    }

    public record DevolucionResponse(
            Long id,
            Long ventaId,
            EstadoVenta estadoVenta,
            Long tiendaId,
            String tiendaNombre,
            Long usuarioId,
            String usuarioNombre,
            Long turnoCajaId,
            MetodoPago metodoPagoReembolso,
            BigDecimal total,
            BigDecimal costoTotal,
            BigDecimal utilidadRevertida,
            Instant fecha,
            String motivo,
            String referencia,
            List<DevolucionDetalleResponse> detalles) {
    }

    public record DevolucionDetalleResponse(
            Long ventaDetalleId,
            Long productoId,
            String productoNombre,
            UnidadVenta unidadVenta,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal) {
    }
}
