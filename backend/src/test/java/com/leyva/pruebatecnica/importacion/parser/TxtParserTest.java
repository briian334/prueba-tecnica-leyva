package com.leyva.pruebatecnica.importacion.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.leyva.pruebatecnica.importacion.modelo.ErrorImportacion;
import com.leyva.pruebatecnica.importacion.modelo.RegistroImportado;
import com.leyva.pruebatecnica.importacion.modelo.ResultadoParseo;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TxtParserTest {

    private final TxtParser parser = new TxtParser();

    @Test
    void parseaTxtValidoConBomYColumnasEnDistintoOrden() throws Exception {
        String contenido = "\uFEFFimporte|cuenta_abono|concepto|fecha|cuenta_cargo|referencia\n"
                + "100.500| 4100-Ventas | Venta de contado |2026-09-16| 1000-Caja | REF-001 ";

        ResultadoParseo resultado = parser.parsear(
                new ByteArrayInputStream(contenido.getBytes(StandardCharsets.UTF_8)));

        assertThat(resultado.errores()).isEmpty();
        assertThat(resultado.registros()).containsExactly(new RegistroImportado(
                LocalDate.of(2026, 9, 16),
                "REF-001",
                "Venta de contado",
                "1000-Caja",
                "4100-Ventas",
                new BigDecimal("100.50")));
    }

    @Test
    void rechazaEncabezadosFaltantesDuplicadosYDesconocidos() throws Exception {
        String contenido = "fecha|referencia|concepto|cuenta_cargo|importe|importe|otra_columna";

        ResultadoParseo resultado = parsear(contenido);

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores())
                .allSatisfy(error -> {
                    assertThat(error.fila()).isNull();
                    assertThat(error.campo()).isNull();
                })
                .extracting(ErrorImportacion::mensaje)
                .containsExactlyInAnyOrder(
                        "Falta el encabezado: cuenta_abono",
                        "Encabezado duplicado: importe",
                        "Encabezado desconocido: otra_columna");
    }

    @Test
    void rechazaEncabezadosConDiferenciasDeMayusculasYMinusculas() throws Exception {
        String contenido = "FECHA|referencia|concepto|cuenta_cargo|cuenta_abono|importe";

        ResultadoParseo resultado = parsear(contenido);

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(null, null, "Falta el encabezado: fecha"),
                new ErrorImportacion(null, null, "Encabezado desconocido: FECHA"));
    }

    @Test
    void acumulaCantidadIncorrectaDeCamposYContinuaConLaSiguienteFila() throws Exception {
        String contenido = "fecha|referencia|concepto|cuenta_cargo|cuenta_abono|importe\n"
                + "   \n"
                + "2026-09-16|REF-001|Venta|1000-Caja|4100-Ventas|100.00|sobrante\n"
                + "2026-09-17|REF-002|Compra|5100-Gastos|2010-Proveedores|25.50";

        ResultadoParseo resultado = parsear(contenido);

        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(3, null, "Número incorrecto de campos: se esperaban 6"));
        assertThat(resultado.registros())
                .extracting(RegistroImportado::referencia)
                .containsExactly("REF-002");
    }

    @Test
    void acumulaErroresDeValidacionComunSinCrearRegistroParcial() throws Exception {
        String contenido = "fecha|referencia|concepto|cuenta_cargo|cuenta_abono|importe\n"
                + "2026-09-16|   |   |   |   |0.000";

        ResultadoParseo resultado = parsear(contenido);

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(2, "referencia", "La referencia es obligatoria"),
                new ErrorImportacion(2, "cuenta_cargo", "La cuenta de cargo es obligatoria"),
                new ErrorImportacion(2, "cuenta_abono", "La cuenta de abono es obligatoria"),
                new ErrorImportacion(2, "importe", "El importe debe ser mayor que cero"));
    }

    @Test
    void acumulaErroresTecnicosDeFechaEImporteEnLaMismaFila() {
        String contenido = "fecha|referencia|concepto|cuenta_cargo|cuenta_abono|importe\n"
                + "2026-02-30|REF-001|Venta|1000-Caja|4100-Ventas|$100.00";

        ResultadoParseo resultado = assertDoesNotThrow(() -> parsear(contenido));

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(2, "fecha", "Fecha inválida"),
                new ErrorImportacion(2, "importe", "Importe inválido"));
    }

    @Test
    void reportaArchivoVacioComoErrorGlobal() {
        ResultadoParseo resultado = assertDoesNotThrow(() -> parsear(""));

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(null, null, "El archivo no contiene encabezados"));
    }

    @Test
    void reportaUtf8InvalidoComoErrorGlobal() throws Exception {
        byte[] contenido = {(byte) 0xC3, 0x28};

        ResultadoParseo resultado = parser.parsear(new ByteArrayInputStream(contenido));

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(null, null, "El archivo TXT no contiene UTF-8 válido"));
    }

    @Test
    void aceptaArchivoConSoloEncabezados() throws Exception {
        ResultadoParseo resultado = parsear(
                "fecha|referencia|concepto|cuenta_cargo|cuenta_abono|importe");

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "100,100.00",
        "100.5,100.50",
        "100.50,100.50",
        "100.500,100.50"
    })
    void aceptaImportesConMaximoDosDecimalesSignificativos(
            String importe, String importeNormalizado) throws Exception {
        String contenido = "fecha|referencia|concepto|cuenta_cargo|cuenta_abono|importe\n"
                + "2026-09-16|REF-001|Venta|1000-Caja|4100-Ventas|" + importe;

        ResultadoParseo resultado = parsear(contenido);

        assertThat(resultado.errores()).isEmpty();
        assertThat(resultado.registros())
                .extracting(RegistroImportado::importe)
                .containsExactly(new BigDecimal(importeNormalizado));
    }

    @Test
    void rechazaImporteConMasDeDosDecimalesSignificativos() throws Exception {
        String contenido = "fecha|referencia|concepto|cuenta_cargo|cuenta_abono|importe\n"
                + "2026-09-16|REF-001|Venta|1000-Caja|4100-Ventas|100.501";

        ResultadoParseo resultado = parsear(contenido);

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(2, "importe", "El importe admite como máximo dos decimales"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1,250.50", "1250,50", "$1250.50", "1E3", "+100"})
    void rechazaFormatosTextualesDeImporteNoPermitidos(String importe) throws Exception {
        String contenido = "fecha|referencia|concepto|cuenta_cargo|cuenta_abono|importe\n"
                + "2026-09-16|REF-001|Venta|1000-Caja|4100-Ventas|" + importe;

        ResultadoParseo resultado = parsear(contenido);

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(2, "importe", "Importe inválido"));
    }

    @Test
    void validaFechaEImporteObligatorios() throws Exception {
        String contenido = "fecha|referencia|concepto|cuenta_cargo|cuenta_abono|importe\n"
                + " |REF-001|Venta|1000-Caja|4100-Ventas| ";

        ResultadoParseo resultado = parsear(contenido);

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(2, "fecha", "La fecha es obligatoria"),
                new ErrorImportacion(2, "importe", "El importe es obligatorio"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-02-30", "2026-9-16", "+12345-01-01"})
    void exigeFechaTextualConFormatoEstricto(String fecha) throws Exception {
        String contenido = "fecha|referencia|concepto|cuenta_cargo|cuenta_abono|importe\n"
                + fecha + "|REF-001|Venta|1000-Caja|4100-Ventas|100";

        ResultadoParseo resultado = parsear(contenido);

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(2, "fecha", "Fecha inválida"));
    }

    @Test
    void rechazaEncabezadoVacioAdicional() throws Exception {
        ResultadoParseo resultado = parsear(
                "fecha|referencia|concepto|cuenta_cargo|cuenta_abono|importe|");

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(null, null, "Encabezado vacío en la columna 7"));
    }

    private ResultadoParseo parsear(String contenido) throws Exception {
        return parser.parsear(new ByteArrayInputStream(contenido.getBytes(StandardCharsets.UTF_8)));
    }
}
