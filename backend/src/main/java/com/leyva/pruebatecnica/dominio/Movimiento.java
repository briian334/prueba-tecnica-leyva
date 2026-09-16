package com.leyva.pruebatecnica.dominio;

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
@Table(name = "movimientos")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poliza_id", nullable = false)
    private Poliza poliza;

    @Column(nullable = false, length = 100)
    private String cuenta;

    @Column(nullable = false, length = 100)
    private String referencia;

    @Column(nullable = false, length = 255)
    private String concepto;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal debe;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal haber;

    protected Movimiento() {
    }

    public Movimiento(
            String cuenta,
            String referencia,
            String concepto,
            BigDecimal debe,
            BigDecimal haber) {
        this.cuenta = cuenta;
        this.referencia = referencia;
        this.concepto = concepto;
        this.debe = debe;
        this.haber = haber;
    }

    void asignarPoliza(Poliza poliza) {
        this.poliza = poliza;
    }

    public Long getId() {
        return id;
    }

    public Poliza getPoliza() {
        return poliza;
    }

    public String getCuenta() {
        return cuenta;
    }

    public String getReferencia() {
        return referencia;
    }

    public String getConcepto() {
        return concepto;
    }

    public BigDecimal getDebe() {
        return debe;
    }

    public BigDecimal getHaber() {
        return haber;
    }
}
