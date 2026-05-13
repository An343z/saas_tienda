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
@Table(name = "turnos_caja")
public class TurnoCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tienda_id", nullable = false)
    private Tienda tienda;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private Instant fechaApertura = Instant.now();

    private Instant fechaCierre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal fondoInicial = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal efectivoEsperado = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal efectivoContado;

    @Column(precision = 12, scale = 2)
    private BigDecimal diferencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTurnoCaja estado = EstadoTurnoCaja.ABIERTO;

    @Column(length = 240)
    private String observaciones;

    protected TurnoCaja() {
    }

    public TurnoCaja(Tienda tienda, Usuario usuario, BigDecimal fondoInicial, String observaciones) {
        this.tienda = tienda;
        this.usuario = usuario;
        this.fondoInicial = fondoInicial;
        this.efectivoEsperado = fondoInicial;
        this.observaciones = observaciones;
    }

    public void sumarEfectivo(BigDecimal monto) {
        efectivoEsperado = efectivoEsperado.add(monto);
    }

    public void restarEfectivo(BigDecimal monto) {
        efectivoEsperado = efectivoEsperado.subtract(monto);
    }

    public void cerrar(BigDecimal efectivoContado, String observacionesCierre) {
        if (estado == EstadoTurnoCaja.CERRADO) {
            throw new IllegalArgumentException("El turno de caja ya esta cerrado");
        }
        this.efectivoContado = efectivoContado;
        this.diferencia = efectivoContado.subtract(efectivoEsperado);
        this.fechaCierre = Instant.now();
        this.estado = EstadoTurnoCaja.CERRADO;
        if (observacionesCierre != null && !observacionesCierre.isBlank()) {
            this.observaciones = observacionesCierre;
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

    public Instant getFechaApertura() {
        return fechaApertura;
    }

    public Instant getFechaCierre() {
        return fechaCierre;
    }

    public BigDecimal getFondoInicial() {
        return fondoInicial;
    }

    public BigDecimal getEfectivoEsperado() {
        return efectivoEsperado;
    }

    public BigDecimal getEfectivoContado() {
        return efectivoContado;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public EstadoTurnoCaja getEstado() {
        return estado;
    }

    public String getObservaciones() {
        return observaciones;
    }
}
