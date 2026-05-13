package com.saas_tienda.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "movimientos_caja")
public class MovimientoCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "turno_caja_id", nullable = false)
    private TurnoCaja turnoCaja;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tienda_id", nullable = false)
    private Tienda tienda;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimientoCaja tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MetodoPago metodoPago;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(length = 240)
    private String descripcion;

    @Column(length = 120)
    private String referencia;

    @Column(nullable = false)
    private Instant fecha = Instant.now();

    protected MovimientoCaja() {
    }

    public MovimientoCaja(TurnoCaja turnoCaja, Usuario usuario, Venta venta, TipoMovimientoCaja tipo, MetodoPago metodoPago, BigDecimal monto, String descripcion, String referencia) {
        this.turnoCaja = turnoCaja;
        this.tienda = turnoCaja.getTienda();
        this.usuario = usuario;
        this.venta = venta;
        this.tipo = tipo;
        this.metodoPago = metodoPago;
        this.monto = monto;
        this.descripcion = descripcion;
        this.referencia = referencia;
    }

    public Long getId() {
        return id;
    }

    public TurnoCaja getTurnoCaja() {
        return turnoCaja;
    }

    public Tienda getTienda() {
        return tienda;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Venta getVenta() {
        return venta;
    }

    public TipoMovimientoCaja getTipo() {
        return tipo;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getReferencia() {
        return referencia;
    }

    public Instant getFecha() {
        return fecha;
    }
}
