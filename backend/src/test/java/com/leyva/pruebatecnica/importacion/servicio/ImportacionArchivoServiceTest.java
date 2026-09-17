package com.leyva.pruebatecnica.importacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.leyva.pruebatecnica.dominio.EstatusPoliza;
import com.leyva.pruebatecnica.dominio.Poliza;
import com.leyva.pruebatecnica.importacion.modelo.ErrorImportacion;
import com.leyva.pruebatecnica.importacion.modelo.RegistroImportado;
import com.leyva.pruebatecnica.importacion.modelo.ResultadoParseo;
import com.leyva.pruebatecnica.importacion.parser.ParserArchivo;
import com.leyva.pruebatecnica.importacion.parser.SeleccionadorParserArchivo;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class ImportacionArchivoServiceTest {

    private ParserArchivo txtParser;
    private ParserArchivo xlsxParser;
    private ImportacionService importacionService;
    private ImportacionArchivoService importacionArchivoService;

    @BeforeEach
    void configurar() {
        txtParser = mock(ParserArchivo.class);
        xlsxParser = mock(ParserArchivo.class);
        importacionService = mock(ImportacionService.class);
        importacionArchivoService = new ImportacionArchivoService(
                new SeleccionadorParserArchivo(txtParser, xlsxParser),
                importacionService);
    }

    @Test
    void normalizaElNombreSeleccionaElParserYDevuelveElResultado() throws IOException {
        MultipartFile archivo = new MockMultipartFile(
                "archivo",
                "C:\\temporal\\operaciones.TXT",
                "text/plain",
                "contenido".getBytes());
        ResultadoParseo resultadoParseo = new ResultadoParseo(
                List.of(new RegistroImportado(
                        LocalDate.of(2026, 9, 16),
                        "REF-001",
                        "Venta",
                        "1000-Caja",
                        "4100-Ventas",
                        new BigDecimal("125.50"))),
                List.of());
        Poliza poliza = mock(Poliza.class);
        when(poliza.getId()).thenReturn(7L);
        when(poliza.getTotalDebe()).thenReturn(new BigDecimal("125.50"));
        when(poliza.getTotalHaber()).thenReturn(new BigDecimal("125.50"));
        when(poliza.getEstatus()).thenReturn(EstatusPoliza.GENERADA);
        when(txtParser.parsear(any())).thenReturn(resultadoParseo);
        when(importacionService.generarYPersistir(resultadoParseo, "operaciones.TXT"))
                .thenReturn(poliza);

        var resultado = importacionArchivoService.importar(archivo);

        assertThat(resultado.idPoliza()).isEqualTo(7L);
        assertThat(resultado.registrosProcesados()).isEqualTo(1);
        assertThat(resultado.totalDebe()).isEqualByComparingTo("125.50");
        assertThat(resultado.totalHaber()).isEqualByComparingTo("125.50");
        assertThat(resultado.estatus()).isEqualTo(EstatusPoliza.GENERADA);
        verifyNoInteractions(xlsxParser);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("archivosInvalidos")
    void rechazaArchivoInvalidoAntesDeParsear(
            String caso,
            MultipartFile archivo,
            String mensajeEsperado) {
        assertThatThrownBy(() -> importacionArchivoService.importar(archivo))
                .isInstanceOfSatisfying(ImportacionInvalidaException.class,
                        excepcion -> assertThat(excepcion.getErrores()).containsExactly(
                                new ErrorImportacion(null, null, mensajeEsperado)));
        verifyNoInteractions(txtParser, xlsxParser, importacionService);
    }

    private static Stream<Arguments> archivosInvalidos() {
        return Stream.of(
                Arguments.of("archivo ausente", null, "El archivo es obligatorio"),
                Arguments.of(
                        "archivo vacío",
                        new MockMultipartFile("archivo", "operaciones.txt", "text/plain", new byte[0]),
                        "El archivo está vacío"),
                Arguments.of(
                        "nombre ausente",
                        new MockMultipartFile("archivo", "", "text/plain", new byte[] {1}),
                        "El archivo debe tener un nombre"),
                Arguments.of(
                        "extensión no soportada",
                        new MockMultipartFile("archivo", "operaciones.csv", "text/csv", new byte[] {1}),
                        "La extensión del archivo no está soportada"),
                Arguments.of(
                        "nombre demasiado largo",
                        new MockMultipartFile(
                                "archivo",
                                "a".repeat(252) + ".txt",
                                "text/plain",
                                new byte[] {1}),
                        "El nombre del archivo no puede exceder 255 caracteres"));
    }
}
