package com.saas_tienda.backend.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tienda_id", nullable = false)
    private Tienda tienda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_caja_id")
    private TurnoCaja turnoCaja;

    @Column(nullable = false)
    private Instant fecha = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoVenta estado = EstadoVenta.VIGENTE;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal utilidad = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPagado = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cambio = BigDecimal.ZERO;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaDetalle> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PagoVenta> pagos = new ArrayList<>();

    protected Venta() {
    }

    public Venta(Tienda tienda, Usuario usuario) {
        this.tienda = tienda;
        this.usuario = usuario;
    }

    public void agregarDetalle(VentaDetalle detalle) {
        detalle.setVenta(this);
        detalles.add(detalle);
        total = total.add(detalle.getSubtotal());
        costoTotal = costoTotal.add(detalle.getCostoTotal());
        utilidad = utilidad.add(detalle.getUtilidad());
    }

    public void agregarPago(PagoVenta pago) {
        pago.setVenta(this);
        pagos.add(pago);
        totalPagado = totalPagado.add(pago.getMonto());
        cambio = cambio.add(pago.getCambio());
    }

    public void setTurnoCaja(TurnoCaja turnoCaja) {
        this.turnoCaja = turnoCaja;
    }

    public void actualizarEstadoPorDevoluciones() {
        boolean todoDevuelto = detalles.stream()
                .allMatch(detalle -> detalle.getCantidadDisponibleDevolucion().compareTo(BigDecimal.ZERO) == 0);
        boolean algoDevuelto = detalles.stream()
                .anyMatch(detalle -> detalle.getCantidadDevuelta().compareTo(BigDecimal.ZERO) > 0);
        if (todoDevuelto) {
            estado = EstadoVenta.CANCELADA;
        } else if (algoDevuelto) {
            estado = EstadoVenta.DEVUELTA_PARCIAL;
        } else {
            estado = EstadoVenta.VIGENTE;
        }
    }

    public Long getId() {
        return id;
    }

    public Tienda getTienda() {
        return tienda;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public TurnoCaja getTurnoCaja() {
        return turnoCaja;
    }

    public Instant getFecha() {
        return fecha;
    }

    public EstadoVenta getEstado() {
        return estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public BigDecimal getUtilidad() {
        return utilidad;
    }

    public BigDecimal getTotalPagado() {
        return totalPagado;
    }

    public BigDecimal getCambio() {
        return cambio;
    }

    public List<VentaDetalle> getDetalles() {
        return detalles;
    }

    public List<PagoVenta> getPagos() {
        return pagos;
    }
}
