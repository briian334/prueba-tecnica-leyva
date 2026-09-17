package com.leyva.pruebatecnica.importacion.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.leyva.pruebatecnica.importacion.modelo.ErrorImportacion;
import com.leyva.pruebatecnica.importacion.modelo.RegistroImportado;
import com.leyva.pruebatecnica.importacion.modelo.ResultadoParseo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class XlsxParserTest {

    private final XlsxParser parser = new XlsxParser();

    @Test
    void parseaXlsxValidoConColumnasEnDistintoOrden() throws Exception {
        byte[] contenido;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("operaciones");
            crearFila(hoja, 0, "importe", "cuenta_abono", "concepto", "fecha", "cuenta_cargo", "referencia");
            Row fila = hoja.createRow(1);
            fila.createCell(0).setCellValue(1250.5);
            fila.createCell(1).setCellValue("4100-Ventas");
            fila.createCell(2).setCellValue(" Venta mostrador ");
            fila.createCell(3).setCellValue("2026-09-16");
            fila.createCell(4).setCellValue("1100-Clientes");
            fila.createCell(5).setCellValue("REF-001");
            contenido = escribir(libro);
        }

        ResultadoParseo resultado = parser.parsear(new ByteArrayInputStream(contenido));

        assertThat(resultado.errores()).isEmpty();
        assertThat(resultado.registros()).containsExactly(new RegistroImportado(
                LocalDate.of(2026, 9, 16),
                "REF-001",
                "Venta mostrador",
                "1100-Clientes",
                "4100-Ventas",
                new BigDecimal("1250.50")));
    }

    @Test
    void aceptaFechaNativaExcelEImporteTextual() throws Exception {
        byte[] contenido;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("operaciones");
            crearFila(hoja, 0, "fecha", "referencia", "concepto", "cuenta_cargo", "cuenta_abono", "importe");
            Row fila = hoja.createRow(1);
            CellStyle estiloFecha = libro.createCellStyle();
            estiloFecha.setDataFormat(libro.createDataFormat().getFormat("yyyy-mm-dd"));
            fila.createCell(0).setCellValue(LocalDate.of(2024, 2, 29));
            fila.getCell(0).setCellStyle(estiloFecha);
            fila.createCell(1).setCellValue("REF-002");
            fila.createCell(2).setCellValue("Venta");
            fila.createCell(3).setCellValue("1000-Caja");
            fila.createCell(4).setCellValue("4100-Ventas");
            fila.createCell(5).setCellValue("100.500");
            contenido = escribir(libro);
        }

        ResultadoParseo resultado = parser.parsear(new ByteArrayInputStream(contenido));

        assertThat(resultado.errores()).isEmpty();
        assertThat(resultado.registros())
                .extracting(RegistroImportado::fecha, RegistroImportado::importe)
                .containsExactly(tuple(LocalDate.of(2024, 2, 29), new BigDecimal("100.50")));
    }

    @Test
    void exigeExactamenteUnaHoja() throws Exception {
        byte[] sinHojas;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            sinHojas = escribir(libro);
        }
        byte[] dosHojas;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            libro.createSheet("uno");
            libro.createSheet("dos");
            dosHojas = escribir(libro);
        }

        ResultadoParseo resultadoSinHojas = assertDoesNotThrow(
                () -> parser.parsear(new ByteArrayInputStream(sinHojas)));
        ResultadoParseo resultadoDosHojas = assertDoesNotThrow(
                () -> parser.parsear(new ByteArrayInputStream(dosHojas)));

        assertThat(resultadoSinHojas.errores()).containsExactly(
                new ErrorImportacion(null, null, "El archivo XLSX debe contener exactamente una hoja; contiene 0"));
        assertThat(resultadoDosHojas.errores()).containsExactly(
                new ErrorImportacion(null, null, "El archivo XLSX debe contener exactamente una hoja; contiene 2"));
    }

    @Test
    void reportaAusenciaDeEncabezadosComoErrorGlobal() throws Exception {
        byte[] contenido;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            libro.createSheet("operaciones");
            contenido = escribir(libro);
        }

        ResultadoParseo resultado = assertDoesNotThrow(
                () -> parser.parsear(new ByteArrayInputStream(contenido)));

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(null, null, "El archivo no contiene encabezados"));
    }

    @Test
    void ignoraFilasCompletamenteVacias() throws Exception {
        byte[] contenido;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("operaciones");
            crearFila(hoja, 0, "fecha", "referencia", "concepto", "cuenta_cargo", "cuenta_abono", "importe");
            Row filaVacia = hoja.createRow(1);
            filaVacia.createCell(0).setBlank();
            filaVacia.createCell(1).setCellValue("   ");
            crearFila(hoja, 3, "2026-09-18", "REF-003", "Venta", "1000-Caja", "4100-Ventas", "75.00");
            contenido = escribir(libro);
        }

        ResultadoParseo resultado = parser.parsear(new ByteArrayInputStream(contenido));

        assertThat(resultado.errores()).isEmpty();
        assertThat(resultado.registros())
                .extracting(RegistroImportado::referencia)
                .containsExactly("REF-003");
    }

    @Test
    void rechazaTiposNoTextualesEnCamposDeTexto() throws Exception {
        byte[] contenido;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("operaciones");
            crearFila(hoja, 0, "fecha", "referencia", "concepto", "cuenta_cargo", "cuenta_abono", "importe");
            Row fila = hoja.createRow(1);
            fila.createCell(0).setCellValue("2026-09-16");
            fila.createCell(1).setCellValue(1001);
            fila.createCell(2).setCellValue(true);
            fila.createCell(3).setCellFormula("1+1");
            fila.createCell(4).setCellErrorValue(FormulaError.NA.getCode());
            fila.createCell(5).setCellValue(100);
            contenido = escribir(libro);
        }

        ResultadoParseo resultado = assertDoesNotThrow(
                () -> parser.parsear(new ByteArrayInputStream(contenido)));

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(2, "referencia", "Debe ser una celda de texto"),
                new ErrorImportacion(2, "concepto", "Debe ser una celda de texto"),
                new ErrorImportacion(2, "cuenta_cargo", "Debe ser una celda de texto"),
                new ErrorImportacion(2, "cuenta_abono", "Debe ser una celda de texto"));
    }

    @Test
    void reportaXlsxCorruptoComoErrorGlobal() {
        byte[] contenido = "esto no es un xlsx".getBytes();

        ResultadoParseo resultado = assertDoesNotThrow(
                () -> parser.parsear(new ByteArrayInputStream(contenido)));

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(null, null, "El archivo XLSX está corrupto o no es válido"));
    }

    @Test
    void rechazaEncabezadoQueNoSeaTexto() throws Exception {
        byte[] contenido;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("operaciones");
            Row encabezados = hoja.createRow(0);
            encabezados.createCell(0).setCellValue(20260916);
            encabezados.createCell(1).setCellValue("referencia");
            encabezados.createCell(2).setCellValue("concepto");
            encabezados.createCell(3).setCellValue("cuenta_cargo");
            encabezados.createCell(4).setCellValue("cuenta_abono");
            encabezados.createCell(5).setCellValue("importe");
            contenido = escribir(libro);
        }

        ResultadoParseo resultado = assertDoesNotThrow(
                () -> parser.parsear(new ByteArrayInputStream(contenido)));

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(null, null, "Falta el encabezado: fecha"),
                new ErrorImportacion(null, null, "El encabezado de la columna 1 debe ser texto"));
    }

    @Test
    void rechazaEncabezadosFaltantesDuplicadosYDesconocidos() throws Exception {
        byte[] contenido;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("operaciones");
            crearFila(hoja, 0, "fecha", "referencia", "concepto", "cuenta_cargo", "importe", "importe", "otra_columna");
            contenido = escribir(libro);
        }

        ResultadoParseo resultado = parser.parsear(new ByteArrayInputStream(contenido));

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores())
                .extracting(ErrorImportacion::mensaje)
                .containsExactlyInAnyOrder(
                        "Falta el encabezado: cuenta_abono",
                        "Encabezado duplicado: importe",
                        "Encabezado desconocido: otra_columna");
    }

    @Test
    void rechazaEncabezadosConDiferenciasDeMayusculasYMinusculas() throws Exception {
        byte[] contenido;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("operaciones");
            crearFila(hoja, 0, "FECHA", "referencia", "concepto", "cuenta_cargo", "cuenta_abono", "importe");
            contenido = escribir(libro);
        }

        ResultadoParseo resultado = parser.parsear(new ByteArrayInputStream(contenido));

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(null, null, "Falta el encabezado: fecha"),
                new ErrorImportacion(null, null, "Encabezado desconocido: FECHA"));
    }

    @Test
    void rechazaFechaNumericaSinFormatoDeFechaYFormulaDeImporte() throws Exception {
        byte[] contenido;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("operaciones");
            crearFila(hoja, 0, "fecha", "referencia", "concepto", "cuenta_cargo", "cuenta_abono", "importe");
            Row fila = hoja.createRow(1);
            fila.createCell(0).setCellValue(46249);
            fila.createCell(1).setCellValue("REF-001");
            fila.createCell(2).setCellValue("Venta");
            fila.createCell(3).setCellValue("1000-Caja");
            fila.createCell(4).setCellValue("4100-Ventas");
            fila.createCell(5).setCellFormula("50+50");
            contenido = escribir(libro);
        }

        ResultadoParseo resultado = parser.parsear(new ByteArrayInputStream(contenido));

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(2, "fecha", "Fecha inválida"),
                new ErrorImportacion(2, "importe", "Importe inválido"));
    }

    @Test
    void aceptaArchivoConSoloEncabezados() throws Exception {
        byte[] contenido;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("operaciones");
            crearFila(hoja, 0, "fecha", "referencia", "concepto", "cuenta_cargo", "cuenta_abono", "importe");
            contenido = escribir(libro);
        }

        ResultadoParseo resultado = parser.parsear(new ByteArrayInputStream(contenido));

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).isEmpty();
    }

    @Test
    void exigeFormatoEstrictoEnFechaTextual() throws Exception {
        byte[] contenido;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("operaciones");
            crearFila(hoja, 0, "fecha", "referencia", "concepto", "cuenta_cargo", "cuenta_abono", "importe");
            crearFila(hoja, 1, "+12345-01-01", "REF-001", "Venta", "1000-Caja", "4100-Ventas", "100");
            contenido = escribir(libro);
        }

        ResultadoParseo resultado = parser.parsear(new ByteArrayInputStream(contenido));

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(2, "fecha", "Fecha inválida"));
    }

    @Test
    void conservaDecimalNumericoSinIntroducirPrecisionBinaria() throws Exception {
        byte[] contenido;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("operaciones");
            crearFila(hoja, 0, "fecha", "referencia", "concepto", "cuenta_cargo", "cuenta_abono", "importe");
            Row fila = hoja.createRow(1);
            fila.createCell(0).setCellValue("2026-09-16");
            fila.createCell(1).setCellValue("REF-001");
            fila.createCell(2).setCellValue("Venta");
            fila.createCell(3).setCellValue("1000-Caja");
            fila.createCell(4).setCellValue("4100-Ventas");
            fila.createCell(5).setCellValue(0.1);
            contenido = escribir(libro);
        }

        ResultadoParseo resultado = parser.parsear(new ByteArrayInputStream(contenido));

        assertThat(resultado.errores()).isEmpty();
        assertThat(resultado.registros())
                .extracting(RegistroImportado::importe)
                .containsExactly(new BigDecimal("0.10"));
    }

    @Test
    void validaCamposFaltantesConNumeroDeFilaReal() throws Exception {
        byte[] contenido;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("operaciones");
            crearFila(hoja, 0, "fecha", "referencia", "concepto", "cuenta_cargo", "cuenta_abono", "importe");
            crearFila(hoja, 3, "", "", "Sólo concepto", "", "", "");
            contenido = escribir(libro);
        }

        ResultadoParseo resultado = parser.parsear(new ByteArrayInputStream(contenido));

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(4, "fecha", "La fecha es obligatoria"),
                new ErrorImportacion(4, "referencia", "La referencia es obligatoria"),
                new ErrorImportacion(4, "cuenta_cargo", "La cuenta de cargo es obligatoria"),
                new ErrorImportacion(4, "cuenta_abono", "La cuenta de abono es obligatoria"),
                new ErrorImportacion(4, "importe", "El importe es obligatorio"));
    }

    @Test
    void rechazaEncabezadoVacioAdicional() throws Exception {
        byte[] contenido;
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("operaciones");
            crearFila(hoja, 0, "fecha", "referencia", "concepto", "cuenta_cargo", "cuenta_abono", "importe");
            hoja.getRow(0).createCell(6).setBlank();
            contenido = escribir(libro);
        }

        ResultadoParseo resultado = parser.parsear(new ByteArrayInputStream(contenido));

        assertThat(resultado.registros()).isEmpty();
        assertThat(resultado.errores()).containsExactly(
                new ErrorImportacion(null, null, "Encabezado vacío en la columna 7"));
    }

    private void crearFila(Sheet hoja, int indice, String... valores) {
        Row fila = hoja.createRow(indice);
        for (int columna = 0; columna < valores.length; columna++) {
            fila.createCell(columna).setCellValue(valores[columna]);
        }
    }

    private byte[] escribir(XSSFWorkbook libro) throws Exception {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        libro.write(salida);
        return salida.toByteArray();
    }
}
