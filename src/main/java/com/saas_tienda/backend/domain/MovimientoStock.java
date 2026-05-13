package com.saas_tienda.backend.domain;

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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "movimientos_stock")
public class MovimientoStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tienda_id", nullable = false)
    private Tienda tienda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimientoStock tipo;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal existenciaPosterior;

    @Column(length = 240)
    private String motivo;

    @Column(nullable = false)
    private Instant fecha = Instant.now();

    protected MovimientoStock() {
    }

    public MovimientoStock(Tienda tienda, Producto producto, Usuario usuario, TipoMovimientoStock tipo, BigDecimal cantidad, BigDecimal existenciaPosterior, String motivo) {
        this.tienda = tienda;
        this.producto = producto;
        this.usuario = usuario;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.existenciaPosterior = existenciaPosterior;
        this.motivo = motivo;
    }

    public Long getId() {
        return id;
    }

    public Tienda getTienda() {
        return tienda;
    }

    public Producto getProducto() {
        return producto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public TipoMovimientoStock getTipo() {
        return tipo;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getExistenciaPosterior() {
        return existenciaPosterior;
    }

    public String getMotivo() {
        return motivo;
    }

    public Instant getFecha() {
        return fecha;
    }
}
