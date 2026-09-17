package com.leyva.pruebatecnica.poliza.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leyva.pruebatecnica.dominio.EstatusPoliza;
import com.leyva.pruebatecnica.dominio.Movimiento;
import com.leyva.pruebatecnica.dominio.Poliza;
import com.leyva.pruebatecnica.persistencia.PolizaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PolizaConsultaServiceTest {

    private PolizaRepository polizaRepository;
    private PolizaConsultaService polizaConsultaService;

    @BeforeEach
    void configurar() {
        polizaRepository = mock(PolizaRepository.class);
        polizaConsultaService = new PolizaConsultaService(polizaRepository);
    }

    @Test
    void devuelveResumenSinConsultarMovimientos() {
        Poliza poliza = crearPolizaMock();
        when(polizaRepository.findAll()).thenReturn(List.of(poliza));

        var resultado = polizaConsultaService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().id()).isEqualTo(15L);
        assertThat(resultado.getFirst().fecha()).isEqualTo(LocalDate.of(2026, 9, 16));
        assertThat(resultado.getFirst().concepto()).isEqualTo("Importación de operaciones");
        assertThat(resultado.getFirst().archivoOrigen()).isEqualTo("operaciones.xlsx");
        assertThat(resultado.getFirst().totalDebe()).isEqualByComparingTo("250.75");
        assertThat(resultado.getFirst().totalHaber()).isEqualByComparingTo("250.75");
        assertThat(resultado.getFirst().estatus()).isEqualTo(EstatusPoliza.GENERADA);
        verify(poliza, never()).getMovimientos();
    }

    @Test
    void obtieneDetalleConMovimientosEnElOrdenRecibido() {
        Poliza poliza = crearPolizaMock();
        Movimiento cargo = crearMovimientoMock(
                31L, "1000-Caja", new BigDecimal("250.75"), new BigDecimal("0.00"));
        Movimiento abono = crearMovimientoMock(
                32L, "4100-Ventas", new BigDecimal("0.00"), new BigDecimal("250.75"));
        when(poliza.getMovimientos()).thenReturn(List.of(cargo, abono));
        when(polizaRepository.buscarDetallePorId(15L)).thenReturn(Optional.of(poliza));

        var resultado = polizaConsultaService.obtenerDetalle(15L);

        assertThat(resultado.id()).isEqualTo(15L);
        assertThat(resultado.fecha()).isEqualTo(LocalDate.of(2026, 9, 16));
        assertThat(resultado.fechaCreacion())
                .isEqualTo(LocalDateTime.of(2026, 9, 16, 18, 30, 45));
        assertThat(resultado.movimientos())
                .extracting(
                        movimiento -> movimiento.id(),
                        movimiento -> movimiento.cuenta(),
                        movimiento -> movimiento.debe(),
                        movimiento -> movimiento.haber())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(
                                31L, "1000-Caja", new BigDecimal("250.75"), new BigDecimal("0.00")),
                        org.assertj.core.groups.Tuple.tuple(
                                32L, "4100-Ventas", new BigDecimal("0.00"), new BigDecimal("250.75")));
        verify(polizaRepository).buscarDetallePorId(15L);
    }

    @Test
    void informaCuandoLaPolizaNoExiste() {
        when(polizaRepository.buscarDetallePorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> polizaConsultaService.obtenerDetalle(99L))
                .isInstanceOf(PolizaNoEncontradaException.class)
                .hasMessage("No se encontró la póliza con id 99");
    }

    private Poliza crearPolizaMock() {
        Poliza poliza = mock(Poliza.class);
        when(poliza.getId()).thenReturn(15L);
        when(poliza.getFecha()).thenReturn(LocalDate.of(2026, 9, 16));
        when(poliza.getConcepto()).thenReturn("Importación de operaciones");
        when(poliza.getArchivoOrigen()).thenReturn("operaciones.xlsx");
        when(poliza.getTotalDebe()).thenReturn(new BigDecimal("250.75"));
        when(poliza.getTotalHaber()).thenReturn(new BigDecimal("250.75"));
        when(poliza.getEstatus()).thenReturn(EstatusPoliza.GENERADA);
        when(poliza.getFechaCreacion()).thenReturn(LocalDateTime.of(2026, 9, 16, 18, 30, 45));
        return poliza;
    }

    private Movimiento crearMovimientoMock(
            Long id, String cuenta, BigDecimal debe, BigDecimal haber) {
        Movimiento movimiento = mock(Movimiento.class);
        when(movimiento.getId()).thenReturn(id);
        when(movimiento.getCuenta()).thenReturn(cuenta);
        when(movimiento.getReferencia()).thenReturn("REF-001");
        when(movimiento.getConcepto()).thenReturn("Venta");
        when(movimiento.getDebe()).thenReturn(debe);
        when(movimiento.getHaber()).thenReturn(haber);
        return movimiento;
    }
}
