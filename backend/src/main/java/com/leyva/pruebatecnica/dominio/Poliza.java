package com.leyva.pruebatecnica.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "polizas")
public class Poliza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 255)
    private String concepto;

    @Column(name = "archivo_origen", nullable = false, length = 255)
    private String archivoOrigen;

    @Column(name = "total_debe", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalDebe;

    @Column(name = "total_haber", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalHaber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstatusPoliza estatus;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(
            mappedBy = "poliza",
            fetch = FetchType.LAZY,
            cascade = CascadeType.PERSIST,
            orphanRemoval = false)
    @OrderBy("id ASC")
    private List<Movimiento> movimientos = new ArrayList<>();

    protected Poliza() {
    }

    public Poliza(
            LocalDate fecha,
            String concepto,
            String archivoOrigen,
            BigDecimal totalDebe,
            BigDecimal totalHaber,
            LocalDateTime fechaCreacion) {
        this.fecha = fecha;
        this.concepto = concepto;
        this.archivoOrigen = archivoOrigen;
        this.totalDebe = totalDebe;
        this.totalHaber = totalHaber;
        this.estatus = EstatusPoliza.GENERADA;
        this.fechaCreacion = fechaCreacion;
    }

    public void agregarMovimiento(Movimiento movimiento) {
        Objects.requireNonNull(movimiento, "El movimiento es obligatorio");
        movimientos.add(movimiento);
        movimiento.asignarPoliza(this);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getConcepto() {
        return concepto;
    }

    public String getArchivoOrigen() {
        return archivoOrigen;
    }

    public BigDecimal getTotalDebe() {
        return totalDebe;
    }

    public BigDecimal getTotalHaber() {
        return totalHaber;
    }

    public EstatusPoliza getEstatus() {
        return estatus;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public List<Movimiento> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }
}
