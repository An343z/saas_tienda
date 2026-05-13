package com.saas_tienda.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ReporteDtos {
    private ReporteDtos() {
    }

    public record VentasReporte(BigDecimal totalIngresos, BigDecimal costoTotal, BigDecimal utilidadBruta, int ventas, List<VentaDtos.VentaResponse> detalle) {
    }

    public record StockReporte(List<StockDtos.MovimientoStockResponse> movimientos) {
    }

    public record UtilidadReporte(BigDecimal ingresos, BigDecimal costoVendido, BigDecimal utilidadBruta, BigDecimal egresos, BigDecimal utilidadNeta, Instant desde, Instant hasta) {
    }
}
