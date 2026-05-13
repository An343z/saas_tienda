package com.saas_tienda.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public final class EgresoDtos {
    private EgresoDtos() {
    }

    public record EgresoRequest(Long tiendaId, @NotBlank String categoria, @NotNull @DecimalMin("0.01") BigDecimal monto, String descripcion) {
    }

    public record EgresoResponse(Long id, Long tiendaId, String tiendaNombre, String categoria, BigDecimal monto, String descripcion, Instant fecha) {
    }
}
