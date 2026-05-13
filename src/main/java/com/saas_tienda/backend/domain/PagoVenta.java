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

@Entity
@Table(name = "pagos_venta")
public class PagoVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MetodoPago metodoPago;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal recibido;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cambio;

    @Column(length = 120)
    private String referencia;

    protected PagoVenta() {
    }

    public PagoVenta(MetodoPago metodoPago, BigDecimal monto, BigDecimal recibido, BigDecimal cambio, String referencia) {
        this.metodoPago = metodoPago;
        this.monto = monto;
        this.recibido = recibido;
        this.cambio = cambio;
        this.referencia = referencia;
    }

    void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Long getId() {
        return id;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public BigDecimal getRecibido() {
        return recibido;
    }

    public BigDecimal getCambio() {
        return cambio;
    }

    public String getReferencia() {
        return referencia;
    }
}
