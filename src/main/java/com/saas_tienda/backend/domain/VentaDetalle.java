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
@Table(name = "venta_detalle")
public class VentaDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false, length = 160)
    private String nombreConcepto;

    @Column(nullable = false, length = 20)
    private String unidadVenta;

    @Column(nullable = false)
    private boolean ventaRapida;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadDevuelta = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal utilidad;

    protected VentaDetalle() {
    }

    public VentaDetalle(Producto producto, BigDecimal cantidad, BigDecimal costoUnitario, BigDecimal precioUnitario) {
        this.producto = producto;
        this.nombreConcepto = producto.getNombre();
        this.unidadVenta = producto.getUnidadVenta().name();
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.precioUnitario = precioUnitario;
        this.subtotal = precioUnitario.multiply(cantidad);
        this.costoTotal = costoUnitario.multiply(cantidad);
        this.utilidad = subtotal.subtract(costoTotal);
    }

    public VentaDetalle(String nombreConcepto, String unidadVenta, BigDecimal cantidad, BigDecimal costoUnitario, BigDecimal precioUnitario) {
        this.nombreConcepto = nombreConcepto;
        this.unidadVenta = unidadVenta;
        this.ventaRapida = true;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.precioUnitario = precioUnitario;
        this.subtotal = precioUnitario.multiply(cantidad);
        this.costoTotal = costoUnitario.multiply(cantidad);
        this.utilidad = subtotal.subtract(costoTotal);
    }

    void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Long getId() {
        return id;
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

    public boolean isVentaRapida() {
        return ventaRapida;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getCantidadDevuelta() {
        return cantidadDevuelta;
    }

    public BigDecimal getCantidadDisponibleDevolucion() {
        return cantidad.subtract(cantidadDevuelta);
    }

    public void registrarDevolucion(BigDecimal cantidadDevolver) {
        if (cantidadDevolver.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a devolver debe ser mayor a cero");
        }
        if (cantidadDevuelta.add(cantidadDevolver).compareTo(cantidad) > 0) {
            throw new IllegalArgumentException("La cantidad a devolver excede lo vendido para " + nombreConcepto);
        }
        cantidadDevuelta = cantidadDevuelta.add(cantidadDevolver);
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

    public BigDecimal getUtilidad() {
        return utilidad;
    }
}
