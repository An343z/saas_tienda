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
@Table(name = "devoluciones_venta")
public class DevolucionVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tienda_id", nullable = false)
    private Tienda tienda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_caja_id")
    private TurnoCaja turnoCaja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MetodoPago metodoPagoReembolso;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal utilidadRevertida = BigDecimal.ZERO;

    @Column(nullable = false)
    private Instant fecha = Instant.now();

    @Column(nullable = false, length = 240)
    private String motivo;

    @Column(length = 120)
    private String referencia;

    @OneToMany(mappedBy = "devolucion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DevolucionVentaDetalle> detalles = new ArrayList<>();

    protected DevolucionVenta() {
    }

    public DevolucionVenta(Venta venta, Usuario usuario, TurnoCaja turnoCaja, MetodoPago metodoPagoReembolso, String motivo, String referencia) {
        this.venta = venta;
        this.tienda = venta.getTienda();
        this.usuario = usuario;
        this.turnoCaja = turnoCaja;
        this.metodoPagoReembolso = metodoPagoReembolso;
        this.motivo = motivo;
        this.referencia = referencia;
    }

    public void agregarDetalle(DevolucionVentaDetalle detalle) {
        detalle.setDevolucion(this);
        detalles.add(detalle);
        total = total.add(detalle.getSubtotal());
        costoTotal = costoTotal.add(detalle.getCostoTotal());
        utilidadRevertida = utilidadRevertida.add(detalle.getUtilidadRevertida());
    }

    public Long getId() {
        return id;
    }

    public Venta getVenta() {
        return venta;
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

    public MetodoPago getMetodoPagoReembolso() {
        return metodoPagoReembolso;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public BigDecimal getUtilidadRevertida() {
        return utilidadRevertida;
    }

    public Instant getFecha() {
        return fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getReferencia() {
        return referencia;
    }

    public List<DevolucionVentaDetalle> getDetalles() {
        return detalles;
    }
}
