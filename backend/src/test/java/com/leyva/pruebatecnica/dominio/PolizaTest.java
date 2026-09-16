package com.leyva.pruebatecnica.dominio;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PolizaTest {

    @Test
    void agregarMovimientoMantieneAmbosLadosSincronizados() {
        Poliza poliza = crearPoliza();
        Movimiento movimiento = new Movimiento(
                "1100-Clientes",
                "REF-001",
                "Venta",
                new BigDecimal("100.00"),
                new BigDecimal("0.00"));

        poliza.agregarMovimiento(movimiento);

        assertThat(poliza.getMovimientos()).containsExactly(movimiento);
        assertThat(movimiento.getPoliza()).isSameAs(poliza);
    }

    @Test
    void agregarMovimientoRechazaMovimientoNulo() {
        Poliza poliza = crearPoliza();

        assertThatNullPointerException()
                .isThrownBy(() -> poliza.agregarMovimiento(null));
    }

    private Poliza crearPoliza() {
        return new Poliza(
                LocalDate.of(2026, 9, 16),
                "Importacion contable",
                "operaciones.txt",
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                LocalDateTime.of(2026, 9, 16, 12, 0));
    }
}
