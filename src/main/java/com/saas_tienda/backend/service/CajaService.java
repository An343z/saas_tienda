package com.saas_tienda.backend.service;

import com.saas_tienda.backend.domain.EstadoTurnoCaja;
import com.saas_tienda.backend.domain.DevolucionVenta;
import com.saas_tienda.backend.domain.MetodoPago;
import com.saas_tienda.backend.domain.MovimientoCaja;
import com.saas_tienda.backend.domain.PagoVenta;
import com.saas_tienda.backend.domain.Tienda;
import com.saas_tienda.backend.domain.TipoMovimientoCaja;
import com.saas_tienda.backend.domain.TurnoCaja;
import com.saas_tienda.backend.domain.Usuario;
import com.saas_tienda.backend.domain.Venta;
import com.saas_tienda.backend.dto.CajaDtos;
import com.saas_tienda.backend.repository.MovimientoCajaRepository;
import com.saas_tienda.backend.repository.TiendaRepository;
import com.saas_tienda.backend.repository.TurnoCajaRepository;
import com.saas_tienda.backend.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CajaService {
    private final TurnoCajaRepository turnoCajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final TiendaRepository tiendaRepository;
    private final UsuarioRepository usuarioRepository;

    public CajaService(
            TurnoCajaRepository turnoCajaRepository,
            MovimientoCajaRepository movimientoCajaRepository,
            TiendaRepository tiendaRepository,
            UsuarioRepository usuarioRepository) {
        this.turnoCajaRepository = turnoCajaRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.tiendaRepository = tiendaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public TurnoCaja abrir(CajaDtos.AbrirTurnoRequest request) {
        Tienda tienda = tiendaRepository.findById(request.tiendaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Tienda no encontrada"));
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (turnoCajaRepository.existsByTiendaIdAndUsuarioIdAndEstado(tienda.getId(), usuario.getId(), EstadoTurnoCaja.ABIERTO)) {
            throw new IllegalArgumentException("El usuario ya tiene un turno de caja abierto en esta tienda");
        }
        TurnoCaja turno = turnoCajaRepository.save(new TurnoCaja(tienda, usuario, request.fondoInicial(), request.observaciones()));
        movimientoCajaRepository.save(new MovimientoCaja(
                turno,
                usuario,
                null,
                TipoMovimientoCaja.APERTURA,
                MetodoPago.EFECTIVO,
                request.fondoInicial(),
                "Fondo inicial",
                null));
        return turno;
    }

    @Transactional
    public TurnoCaja cerrar(Long turnoCajaId, CajaDtos.CerrarTurnoRequest request) {
        TurnoCaja turno = turnoCajaRepository.findLockedById(turnoCajaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno de caja no encontrado"));
        turno.cerrar(request.efectivoContado(), request.observaciones());
        return turno;
    }

    @Transactional(readOnly = true)
    public TurnoCaja buscar(Long turnoCajaId) {
        return turnoCajaRepository.findById(turnoCajaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno de caja no encontrado"));
    }

    @Transactional(readOnly = true)
    public TurnoCaja actual(Long tiendaId, Long usuarioId) {
        return turnoCajaRepository.findFirstByTiendaIdAndUsuarioIdAndEstadoOrderByFechaAperturaDesc(tiendaId, usuarioId, EstadoTurnoCaja.ABIERTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay turno de caja abierto para este usuario"));
    }

    @Transactional(readOnly = true)
    public List<TurnoCaja> listarPorTienda(Long tiendaId) {
        return turnoCajaRepository.findByTiendaIdOrderByFechaAperturaDesc(tiendaId);
    }

    @Transactional
    public MovimientoCaja registrarMovimiento(CajaDtos.MovimientoCajaRequest request) {
        TurnoCaja turno = turnoAbierto(request.turnoCajaId());
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        TipoMovimientoCaja tipo = request.tipo();
        if (tipo == TipoMovimientoCaja.APERTURA || tipo == TipoMovimientoCaja.VENTA || tipo == TipoMovimientoCaja.DEVOLUCION) {
            throw new IllegalArgumentException("Este endpoint solo acepta entradas, retiros, egresos o ajustes manuales");
        }
        MetodoPago metodoPago = request.metodoPago() == null ? MetodoPago.EFECTIVO : request.metodoPago();
        MovimientoCaja movimiento = new MovimientoCaja(turno, usuario, null, tipo, metodoPago, request.monto(), request.descripcion(), request.referencia());
        afectarEfectivo(turno, tipo, metodoPago, request.monto());
        return movimientoCajaRepository.save(movimiento);
    }

    @Transactional(readOnly = true)
    public List<MovimientoCaja> movimientos(Long turnoCajaId) {
        return movimientoCajaRepository.findByTurnoCajaIdOrderByFechaAsc(turnoCajaId);
    }

    @Transactional
    public TurnoCaja resolverTurnoParaVenta(Tienda tienda, Usuario usuario, Long turnoCajaId, boolean requerido) {
        if (turnoCajaId != null) {
            TurnoCaja turno = turnoAbierto(turnoCajaId);
            if (!turno.getTienda().getId().equals(tienda.getId())) {
                throw new IllegalArgumentException("El turno de caja no pertenece a la tienda de la venta");
            }
            return turno;
        }
        return turnoCajaRepository.findFirstLockedByTiendaIdAndUsuarioIdAndEstadoOrderByFechaAperturaDesc(tienda.getId(), usuario.getId(), EstadoTurnoCaja.ABIERTO)
                .orElseGet(() -> {
                    if (requerido) {
                        throw new IllegalArgumentException("Abre un turno de caja antes de vender en POS");
                    }
                    return null;
                });
    }

    @Transactional
    public void registrarVenta(Venta venta) {
        TurnoCaja turno = venta.getTurnoCaja();
        if (turno == null) {
            return;
        }
        for (PagoVenta pago : venta.getPagos()) {
            movimientoCajaRepository.save(new MovimientoCaja(
                    turno,
                    venta.getUsuario(),
                    venta,
                    TipoMovimientoCaja.VENTA,
                    pago.getMetodoPago(),
                    pago.getMonto(),
                    "Venta " + venta.getId(),
                    pago.getReferencia()));
            afectarEfectivo(turno, TipoMovimientoCaja.VENTA, pago.getMetodoPago(), pago.getMonto());
        }
    }

    @Transactional
    public void registrarDevolucion(DevolucionVenta devolucion) {
        TurnoCaja turno = devolucion.getTurnoCaja();
        if (turno == null) {
            return;
        }
        movimientoCajaRepository.save(new MovimientoCaja(
                turno,
                devolucion.getUsuario(),
                devolucion.getVenta(),
                TipoMovimientoCaja.DEVOLUCION,
                devolucion.getMetodoPagoReembolso(),
                devolucion.getTotal(),
                "Devolucion venta " + devolucion.getVenta().getId(),
                devolucion.getReferencia()));
        afectarEfectivo(turno, TipoMovimientoCaja.DEVOLUCION, devolucion.getMetodoPagoReembolso(), devolucion.getTotal());
    }

    public CajaDtos.TurnoCajaResponse toResponse(TurnoCaja turno, boolean incluirMovimientos) {
        List<CajaDtos.MovimientoCajaResponse> movimientos = incluirMovimientos
                ? movimientos(turno.getId()).stream().map(this::toResponse).toList()
                : List.of();
        return new CajaDtos.TurnoCajaResponse(
                turno.getId(),
                turno.getTienda().getId(),
                turno.getTienda().getNombre(),
                turno.getUsuario().getId(),
                turno.getUsuario().getNombre(),
                turno.getFechaApertura(),
                turno.getFechaCierre(),
                turno.getFondoInicial(),
                turno.getEfectivoEsperado(),
                turno.getEfectivoContado(),
                turno.getDiferencia(),
                turno.getEstado(),
                turno.getObservaciones(),
                movimientos);
    }

    public CajaDtos.MovimientoCajaResponse toResponse(MovimientoCaja movimiento) {
        return new CajaDtos.MovimientoCajaResponse(
                movimiento.getId(),
                movimiento.getTurnoCaja().getId(),
                movimiento.getTienda().getId(),
                movimiento.getTienda().getNombre(),
                movimiento.getUsuario().getId(),
                movimiento.getUsuario().getNombre(),
                movimiento.getVenta() == null ? null : movimiento.getVenta().getId(),
                movimiento.getTipo(),
                movimiento.getMetodoPago(),
                movimiento.getMonto(),
                movimiento.getDescripcion(),
                movimiento.getReferencia(),
                movimiento.getFecha());
    }

    private TurnoCaja turnoAbierto(Long turnoCajaId) {
        TurnoCaja turno = turnoCajaRepository.findLockedById(turnoCajaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno de caja no encontrado"));
        if (turno.getEstado() != EstadoTurnoCaja.ABIERTO) {
            throw new IllegalArgumentException("El turno de caja esta cerrado");
        }
        return turno;
    }

    private void afectarEfectivo(TurnoCaja turno, TipoMovimientoCaja tipo, MetodoPago metodoPago, BigDecimal monto) {
        if (metodoPago != MetodoPago.EFECTIVO) {
            return;
        }
        if (tipo == TipoMovimientoCaja.VENTA || tipo == TipoMovimientoCaja.ENTRADA || tipo == TipoMovimientoCaja.AJUSTE) {
            turno.sumarEfectivo(monto);
        } else if (tipo == TipoMovimientoCaja.RETIRO || tipo == TipoMovimientoCaja.EGRESO || tipo == TipoMovimientoCaja.DEVOLUCION) {
            turno.restarEfectivo(monto);
        }
    }
}
