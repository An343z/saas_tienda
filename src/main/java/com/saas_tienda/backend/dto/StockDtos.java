package com.saas_tienda.backend.dto;

import com.saas_tienda.backend.domain.TipoMovimientoStock;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public final class StockDtos {
    private StockDtos() {
    }

    public record RestockRequest(Long tiendaId, @NotNull Long productoId, @DecimalMin(value = "0.001") BigDecimal cantidad, String motivo) {
    }

    public record AjusteStockRequest(Long tiendaId, @NotNull Long productoId, @DecimalMin(value = "0.000") BigDecimal nuevaExistencia, String motivo) {
    }

    public record InventarioResponse(Long tiendaId, String tiendaNombre, Long productoId, String sku, String productoNombre, BigDecimal costoBase, BigDecimal precioBase, BigDecimal existencia) {
    }

    public record MovimientoStockResponse(Long id, Long tiendaId, String tiendaNombre, Long productoId, String productoNombre, TipoMovimientoStock tipo, BigDecimal cantidad, BigDecimal existenciaPosterior, String motivo, Instant fecha) {
    }
}
