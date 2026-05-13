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
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(name = "inventario_tienda", uniqueConstraints = @UniqueConstraint(columnNames = {"tienda_id", "producto_id"}))
public class InventarioTienda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tienda_id", nullable = false)
    private Tienda tienda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal existencia = BigDecimal.ZERO;

    protected InventarioTienda() {
    }

    public InventarioTienda(Tienda tienda, Producto producto, BigDecimal existencia) {
        this.tienda = tienda;
        this.producto = producto;
        this.existencia = existencia;
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

    public BigDecimal getExistencia() {
        return existencia;
    }

    public void sumar(BigDecimal cantidad) {
        existencia = existencia.add(cantidad);
    }

    public void restar(BigDecimal cantidad) {
        if (existencia.compareTo(cantidad) < 0) {
            throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre());
        }
        existencia = existencia.subtract(cantidad);
    }

    public void ajustarA(BigDecimal nuevaExistencia) {
        if (nuevaExistencia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La existencia no puede ser negativa");
        }
        existencia = nuevaExistencia;
    }
}
