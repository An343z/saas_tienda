package com.saas_tienda.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String sku;

    @Column(unique = true, length = 80)
    private String codigoBarras;

    @Column(nullable = false, length = 160)
    private String nombre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoBase;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnidadVenta unidadVenta = UnidadVenta.PIEZA;

    @Column(nullable = false)
    private boolean controlaInventario = true;

    @Column(nullable = false)
    private boolean activo = true;

    protected Producto() {
    }

    public Producto(String sku, String nombre, BigDecimal costoBase, BigDecimal precioBase) {
        this.sku = sku;
        this.nombre = nombre;
        this.costoBase = costoBase;
        this.precioBase = precioBase;
    }

    public Producto(String sku, String codigoBarras, String nombre, BigDecimal costoBase, BigDecimal precioBase) {
        this.sku = sku;
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.costoBase = costoBase;
        this.precioBase = precioBase;
    }

    public Producto(String sku, String codigoBarras, String nombre, BigDecimal costoBase, BigDecimal precioBase, UnidadVenta unidadVenta, boolean controlaInventario) {
        this.sku = sku;
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.costoBase = costoBase;
        this.precioBase = precioBase;
        this.unidadVenta = unidadVenta == null ? UnidadVenta.PIEZA : unidadVenta;
        this.controlaInventario = controlaInventario;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getCostoBase() {
        return costoBase;
    }

    public void setCostoBase(BigDecimal costoBase) {
        this.costoBase = costoBase;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public UnidadVenta getUnidadVenta() {
        return unidadVenta;
    }

    public void setUnidadVenta(UnidadVenta unidadVenta) {
        this.unidadVenta = unidadVenta == null ? UnidadVenta.PIEZA : unidadVenta;
    }

    public boolean isControlaInventario() {
        return controlaInventario;
    }

    public void setControlaInventario(boolean controlaInventario) {
        this.controlaInventario = controlaInventario;
    }

    public boolean aceptaCantidadDecimal() {
        return unidadVenta != UnidadVenta.PIEZA;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
