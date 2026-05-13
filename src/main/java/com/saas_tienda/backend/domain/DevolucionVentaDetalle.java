package com.saas_tienda.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "devolucion_venta_detalle")
public class DevolucionVentaDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "devolucion_id", nullable = false)
    private DevolucionVenta devolucion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_detalle_id", nullable = false)
    private VentaDetalle ventaDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false, length = 160)
    private String nombreConcepto;

    @Column(nullable = false, length = 20)
    private String unidadVenta;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal utilidadRevertida;

    protected DevolucionVentaDetalle() {
    }

    public DevolucionVentaDetalle(VentaDetalle ventaDetalle, BigDecimal cantidad) {
        this.ventaDetalle = ventaDetalle;
        this.producto = ventaDetalle.getProducto();
        this.nombreConcepto = ventaDetalle.getNombreConcepto();
        this.unidadVenta = ventaDetalle.getUnidadVenta();
        this.cantidad = cantidad;
        this.costoUnitario = ventaDetalle.getCostoUnitario();
        this.precioUnitario = ventaDetalle.getPrecioUnitario();
        this.subtotal = precioUnitario.multiply(cantidad);
        this.costoTotal = costoUnitario.multiply(cantidad);
        this.utilidadRevertida = subtotal.subtract(costoTotal);
    }

    void setDevolucion(DevolucionVenta devolucion) {
        this.devolucion = devolucion;
    }

    public Long getId() {
        return id;
    }

    public VentaDetalle getVentaDetalle() {
        return ventaDetalle;
    }

    public Producto getProducto() {
        return producto;
    }

    public String getNombreConcepto() {
        return nombreConcepto;
    }

    public String getUnidadVenta() {
        return unidadVenta;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public BigDecimal getUtilidadRevertida() {
        return utilidadRevertida;
    }
}
