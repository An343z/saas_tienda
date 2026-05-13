package com.saas_tienda.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.saas_tienda.backend.domain.UnidadVenta;
import java.math.BigDecimal;

public final class ProductoDtos {
    private ProductoDtos() {
    }

    public record ProductoRequest(
            @NotBlank String sku,
            String codigoBarras,
            @NotBlank String nombre,
            @NotNull @DecimalMin("0.00") BigDecimal costoBase,
            @NotNull @DecimalMin("0.00") BigDecimal precioBase,
            UnidadVenta unidadVenta,
            Boolean controlaInventario,
            Boolean activo) {
    }

    public record ProductoResponse(Long id, String sku, String codigoBarras, String nombre, BigDecimal costoBase, BigDecimal precioBase, UnidadVenta unidadVenta, boolean controlaInventario, boolean activo) {
    }
}
