package com.leyva.pruebatecnica.importacion.parser;

import com.leyva.pruebatecnica.importacion.modelo.ErrorImportacion;
import com.leyva.pruebatecnica.importacion.modelo.RegistroImportado;
import com.leyva.pruebatecnica.importacion.modelo.ResultadoParseo;
import com.leyva.pruebatecnica.importacion.validacion.FilaImportacion;
import com.leyva.pruebatecnica.importacion.validacion.ValidadorRegistroImportado;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TxtParser implements ParserArchivo {

    private final ValidadorRegistroImportado validador = new ValidadorRegistroImportado();

    @Override
    public ResultadoParseo parsear(InputStream archivo) throws IOException {
        var decodificador = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        BufferedReader lector = new BufferedReader(new InputStreamReader(archivo, decodificador));
        try {
            return parsear(lector);
        } catch (CharacterCodingException ex) {
            return new ResultadoParseo(
                    List.of(),
                    List.of(new ErrorImportacion(
                            null, null, "El archivo TXT no contiene UTF-8 válido")));
        }
    }

    private ResultadoParseo parsear(BufferedReader lector) throws IOException {
        String encabezado = lector.readLine();
        if (encabezado != null && encabezado.startsWith("\uFEFF")) {
            encabezado = encabezado.substring(1);
        }
        if (encabezado == null || encabezado.isBlank()) {
            return new ResultadoParseo(
                    List.of(),
                    List.of(new ErrorImportacion(null, null, "El archivo no contiene encabezados")));
        }

        EncabezadosImportacion.ResultadoEncabezados resultadoEncabezados =
                EncabezadosImportacion.resolver(Arrays.asList(encabezado.split("\\|", -1)));
        if (!resultadoEncabezados.errores().isEmpty()) {
            return new ResultadoParseo(List.of(), resultadoEncabezados.errores());
        }

        List<RegistroImportado> registros = new ArrayList<>();
        List<ErrorImportacion> errores = new ArrayList<>();
        String linea;
        int numeroFila = 1;
        while ((linea = lector.readLine()) != null) {
            numeroFila++;
            if (linea.isBlank()) {
                continue;
            }
            String[] valores = linea.split("\\|", -1);
            if (valores.length != EncabezadosImportacion.ESPERADOS.size()) {
                errores.add(new ErrorImportacion(
                        numeroFila,
                        null,
                        "Número incorrecto de campos: se esperaban 6"));
                continue;
            }
            List<ErrorImportacion> erroresFila = new ArrayList<>();
            LocalDate fecha = parsearFecha(
                    valores[resultadoEncabezados.indices().get("fecha")], numeroFila, erroresFila);
            BigDecimal importe = parsearImporte(
                    valores[resultadoEncabezados.indices().get("importe")], numeroFila, erroresFila);
            if (!erroresFila.isEmpty()) {
                errores.addAll(erroresFila);
                continue;
            }

            FilaImportacion fila = new FilaImportacion(
                    numeroFila,
                    fecha,
                    valores[resultadoEncabezados.indices().get("referencia")],
                    valores[resultadoEncabezados.indices().get("concepto")],
                    valores[resultadoEncabezados.indices().get("cuenta_cargo")],
                    valores[resultadoEncabezados.indices().get("cuenta_abono")],
                    importe);
            ValidadorRegistroImportado.ResultadoValidacion validacion = validador.validar(fila);
            errores.addAll(validacion.errores());
            if (validacion.registro() != null) {
                registros.add(validacion.registro());
            }
        }
        return new ResultadoParseo(registros, errores);
    }

    private LocalDate parsearFecha(
            String valor, int numeroFila, List<ErrorImportacion> errores) {
        try {
            return ConversorTextoImportacion.convertirFecha(valor);
        } catch (DateTimeParseException ex) {
            errores.add(new ErrorImportacion(numeroFila, "fecha", "Fecha inválida"));
            return null;
        }
    }

    private BigDecimal parsearImporte(
            String valor, int numeroFila, List<ErrorImportacion> errores) {
        try {
            return ConversorTextoImportacion.convertirImporte(valor);
        } catch (NumberFormatException ex) {
            errores.add(new ErrorImportacion(numeroFila, "importe", "Importe inválido"));
            return null;
        }
    }
}
