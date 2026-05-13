package com.saas_tienda.backend.dto;

import com.saas_tienda.backend.domain.EstadoTurnoCaja;
import com.saas_tienda.backend.domain.MetodoPago;
import com.saas_tienda.backend.domain.TipoMovimientoCaja;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class CajaDtos {
    private CajaDtos() {
    }

    public record AbrirTurnoRequest(
            @NotNull Long tiendaId,
            @NotNull Long usuarioId,
            @NotNull @DecimalMin(value = "0.00") BigDecimal fondoInicial,
            String observaciones) {
    }

    public record CerrarTurnoRequest(
            @NotNull @DecimalMin(value = "0.00") BigDecimal efectivoContado,
            String observaciones) {
    }

    public record MovimientoCajaRequest(
            @NotNull Long turnoCajaId,
            @NotNull Long usuarioId,
            @NotNull TipoMovimientoCaja tipo,
            MetodoPago metodoPago,
            @NotNull @DecimalMin(value = "0.01") BigDecimal monto,
            String descripcion,
            String referencia) {
    }

    public record TurnoCajaResponse(
            Long id,
            Long tiendaId,
            String tiendaNombre,
            Long usuarioId,
            String usuarioNombre,
            Instant fechaApertura,
            Instant fechaCierre,
            BigDecimal fondoInicial,
            BigDecimal efectivoEsperado,
            BigDecimal efectivoContado,
            BigDecimal diferencia,
            EstadoTurnoCaja estado,
            String observaciones,
            List<MovimientoCajaResponse> movimientos) {
    }

    public record MovimientoCajaResponse(
            Long id,
            Long turnoCajaId,
            Long tiendaId,
            String tiendaNombre,
            Long usuarioId,
            String usuarioNombre,
            Long ventaId,
            TipoMovimientoCaja tipo,
            MetodoPago metodoPago,
            BigDecimal monto,
            String descripcion,
            String referencia,
            Instant fecha) {
    }
}
