package com.leyva.pruebatecnica.importacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.leyva.pruebatecnica.dominio.EstatusPoliza;
import com.leyva.pruebatecnica.dominio.Poliza;
import com.leyva.pruebatecnica.importacion.modelo.ErrorImportacion;
import com.leyva.pruebatecnica.importacion.modelo.RegistroImportado;
import com.leyva.pruebatecnica.importacion.modelo.ResultadoParseo;
import com.leyva.pruebatecnica.persistencia.PolizaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImportacionServiceTest {

    private PolizaRepository polizaRepository;
    private ImportacionService importacionService;

    @BeforeEach
    void configurar() {
        polizaRepository = mock(PolizaRepository.class);
        importacionService = new ImportacionService(polizaRepository);
    }

    @Test
    void generaPolizaBalanceadaConDosMovimientosPorRegistroYPersisteElAgregado() {
        LocalDate fecha = LocalDate.of(2026, 9, 16);
        ResultadoParseo resultadoParseo = new ResultadoParseo(
                List.of(
                        new RegistroImportado(
                                fecha,
                                "REF-001",
                                "Venta de contado",
                                "1000-Caja",
                                "4100-Ventas",
                                new BigDecimal("100.50")),
                        new RegistroImportado(
                                fecha,
                                "REF-002",
                                "Pago de servicio",
                                "5100-Gastos",
                                "2000-Bancos",
                                new BigDecimal("25.00"))),
                List.of());
        when(polizaRepository.save(any(Poliza.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        Poliza poliza = importacionService.generarYPersistir(resultadoParseo, "operaciones.xlsx");

        assertThat(poliza.getFecha()).isEqualTo(fecha);
        assertThat(poliza.getConcepto()).isEqualTo("Importación de operaciones");
        assertThat(poliza.getArchivoOrigen()).isEqualTo("operaciones.xlsx");
        assertThat(poliza.getTotalDebe()).isEqualByComparingTo("125.50");
        assertThat(poliza.getTotalHaber()).isEqualByComparingTo("125.50");
        assertThat(poliza.getEstatus()).isEqualTo(EstatusPoliza.GENERADA);
        assertThat(poliza.getFechaCreacion()).isNotNull();
        assertThat(poliza.getMovimientos())
                .extracting(
                        movimiento -> movimiento.getCuenta(),
                        movimiento -> movimiento.getReferencia(),
                        movimiento -> movimiento.getConcepto(),
                        movimiento -> movimiento.getDebe(),
                        movimiento -> movimiento.getHaber())
                .containsExactly(
                        tuple("1000-Caja", "REF-001", "Venta de contado",
                                new BigDecimal("100.50"), new BigDecimal("0.00")),
                        tuple("4100-Ventas", "REF-001", "Venta de contado",
                                new BigDecimal("0.00"), new BigDecimal("100.50")),
                        tuple("5100-Gastos", "REF-002", "Pago de servicio",
                                new BigDecimal("25.00"), new BigDecimal("0.00")),
                        tuple("2000-Bancos", "REF-002", "Pago de servicio",
                                new BigDecimal("0.00"), new BigDecimal("25.00")));
        assertThat(poliza.getMovimientos())
                .allSatisfy(movimiento -> assertThat(movimiento.getPoliza()).isSameAs(poliza));
        verify(polizaRepository).save(poliza);
    }

    @Test
    void abortaSinPersistirCuandoElParseoContieneErrores() {
        ErrorImportacion error = new ErrorImportacion(3, "importe", "Importe inválido");
        ResultadoParseo resultadoParseo = new ResultadoParseo(
                List.of(new RegistroImportado(
                        LocalDate.of(2026, 9, 16),
                        "REF-001",
                        "Venta",
                        "1000-Caja",
                        "4100-Ventas",
                        new BigDecimal("100.00"))),
                List.of(error));

        assertThatThrownBy(() -> importacionService.generarYPersistir(resultadoParseo, "operaciones.txt"))
                .isInstanceOfSatisfying(ImportacionInvalidaException.class,
                        excepcion -> assertThat(excepcion.getErrores()).containsExactly(error));
        verifyNoInteractions(polizaRepository);
    }

    @Test
    void rechazaResultadoSinRegistrosSinPersistir() {
        ResultadoParseo resultadoParseo = new ResultadoParseo(List.of(), List.of());

        assertThatThrownBy(() -> importacionService.generarYPersistir(resultadoParseo, "operaciones.txt"))
                .isInstanceOfSatisfying(ImportacionInvalidaException.class,
                        excepcion -> assertThat(excepcion.getErrores()).containsExactly(
                                new ErrorImportacion(null, null, "El archivo no contiene operaciones")));
        verifyNoInteractions(polizaRepository);
    }

    @Test
    void rechazaRegistrosConFechasDistintasSinPersistir() {
        ResultadoParseo resultadoParseo = new ResultadoParseo(
                List.of(
                        crearRegistro(LocalDate.of(2026, 9, 16), "REF-001"),
                        crearRegistro(LocalDate.of(2026, 9, 17), "REF-002")),
                List.of());

        assertThatThrownBy(() -> importacionService.generarYPersistir(resultadoParseo, "operaciones.txt"))
                .isInstanceOfSatisfying(ImportacionInvalidaException.class,
                        excepcion -> assertThat(excepcion.getErrores()).containsExactly(
                                new ErrorImportacion(
                                        null,
                                        "fecha",
                                        "Todas las operaciones deben tener la misma fecha")));
        verifyNoInteractions(polizaRepository);
    }

    private RegistroImportado crearRegistro(LocalDate fecha, String referencia) {
        return new RegistroImportado(
                fecha,
                referencia,
                "Venta",
                "1000-Caja",
                "4100-Ventas",
                new BigDecimal("100.00"));
    }
}
