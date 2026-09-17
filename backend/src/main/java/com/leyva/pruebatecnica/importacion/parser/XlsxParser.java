package com.leyva.pruebatecnica.importacion.parser;

import com.leyva.pruebatecnica.importacion.modelo.ErrorImportacion;
import com.leyva.pruebatecnica.importacion.modelo.RegistroImportado;
import com.leyva.pruebatecnica.importacion.modelo.ResultadoParseo;
import com.leyva.pruebatecnica.importacion.validacion.FilaImportacion;
import com.leyva.pruebatecnica.importacion.validacion.ValidadorRegistroImportado;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XlsxParser implements ParserArchivo {

    private final ValidadorRegistroImportado validador = new ValidadorRegistroImportado();

    @Override
    public ResultadoParseo parsear(InputStream archivo) throws IOException {
        byte[] contenido = archivo.readAllBytes();
        if (contenido.length == 0) {
            return new ResultadoParseo(
                    List.of(),
                    List.of(new ErrorImportacion(null, null, "El archivo XLSX está vacío")));
        }

        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(contenido))) {
            int cantidadHojas = libro.getNumberOfSheets();
            if (cantidadHojas != 1) {
                return new ResultadoParseo(
                        List.of(),
                        List.of(new ErrorImportacion(
                                null,
                                null,
                                "El archivo XLSX debe contener exactamente una hoja; contiene "
                                        + cantidadHojas)));
            }
            Sheet hoja = libro.getSheetAt(0);
            Row filaEncabezados = hoja.getRow(0);
            if (filaEncabezados == null || filaEncabezados.getLastCellNum() < 1) {
                return new ResultadoParseo(
                        List.of(),
                        List.of(new ErrorImportacion(
                                null, null, "El archivo no contiene encabezados")));
            }
            List<String> encabezados = new ArrayList<>();
            List<ErrorImportacion> erroresEncabezados = new ArrayList<>();
            for (int columna = 0; columna < filaEncabezados.getLastCellNum(); columna++) {
                Cell celda = filaEncabezados.getCell(columna);
                if (celda == null || celda.getCellType() == CellType.BLANK) {
                    encabezados.add("");
                } else if (celda.getCellType() == CellType.STRING) {
                    encabezados.add(celda.getStringCellValue());
                } else {
                    encabezados.add(null);
                    erroresEncabezados.add(new ErrorImportacion(
                            null,
                            null,
                            "El encabezado de la columna " + (columna + 1) + " debe ser texto"));
                }
            }
            EncabezadosImportacion.ResultadoEncabezados resultadoEncabezados =
                    EncabezadosImportacion.resolver(encabezados);
            erroresEncabezados.addAll(0, resultadoEncabezados.errores());
            if (!erroresEncabezados.isEmpty()) {
                return new ResultadoParseo(List.of(), erroresEncabezados);
            }

            List<RegistroImportado> registros = new ArrayList<>();
            List<ErrorImportacion> errores = new ArrayList<>();
            for (int indiceFila = 1; indiceFila <= hoja.getLastRowNum(); indiceFila++) {
                Row fila = hoja.getRow(indiceFila);
                if (fila == null) {
                    continue;
                }
                if (estaVacia(fila, resultadoEncabezados.indices())) {
                    continue;
                }
                List<ErrorImportacion> erroresFila = new ArrayList<>();
                LocalDate fecha = parsearFecha(
                        fila.getCell(resultadoEncabezados.indices().get("fecha")),
                        indiceFila + 1,
                        libro.isDate1904(),
                        erroresFila);
                String referencia = parsearTexto(
                        fila.getCell(resultadoEncabezados.indices().get("referencia")),
                        indiceFila + 1,
                        "referencia",
                        erroresFila);
                String concepto = parsearTexto(
                        fila.getCell(resultadoEncabezados.indices().get("concepto")),
                        indiceFila + 1,
                        "concepto",
                        erroresFila);
                String cuentaCargo = parsearTexto(
                        fila.getCell(resultadoEncabezados.indices().get("cuenta_cargo")),
                        indiceFila + 1,
                        "cuenta_cargo",
                        erroresFila);
                String cuentaAbono = parsearTexto(
                        fila.getCell(resultadoEncabezados.indices().get("cuenta_abono")),
                        indiceFila + 1,
                        "cuenta_abono",
                        erroresFila);
                BigDecimal importe = parsearImporte(
                        fila.getCell(resultadoEncabezados.indices().get("importe")),
                        indiceFila + 1,
                        erroresFila);
                if (!erroresFila.isEmpty()) {
                    errores.addAll(erroresFila);
                    continue;
                }
                FilaImportacion candidata = new FilaImportacion(
                        indiceFila + 1,
                        fecha,
                        referencia,
                        concepto,
                        cuentaCargo,
                        cuentaAbono,
                        importe);
                ValidadorRegistroImportado.ResultadoValidacion validacion = validador.validar(candidata);
                errores.addAll(validacion.errores());
                if (validacion.registro() != null) {
                    registros.add(validacion.registro());
                }
            }
            return new ResultadoParseo(registros, errores);
        } catch (NotOfficeXmlFileException | POIXMLException | IOException ex) {
            return new ResultadoParseo(
                    List.of(),
                    List.of(new ErrorImportacion(
                            null, null, "El archivo XLSX está corrupto o no es válido")));
        }
    }

    private LocalDate parsearFecha(
            Cell celda,
            int numeroFila,
            boolean fechasDesde1904,
            List<ErrorImportacion> errores) {
        if (celda == null || celda.getCellType() == CellType.BLANK) {
            return null;
        }
        if (celda.getCellType() == CellType.STRING) {
            try {
                return ConversorTextoImportacion.convertirFecha(celda.getStringCellValue());
            } catch (DateTimeParseException ex) {
                errores.add(new ErrorImportacion(numeroFila, "fecha", "Fecha inválida"));
                return null;
            }
        }
        if (celda.getCellType() == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(celda)
                && DateUtil.isValidExcelDate(celda.getNumericCellValue())) {
            return DateUtil.getLocalDateTime(celda.getNumericCellValue(), fechasDesde1904).toLocalDate();
        }
        errores.add(new ErrorImportacion(numeroFila, "fecha", "Fecha inválida"));
        return null;
    }

    private BigDecimal parsearImporte(
            Cell celda, int numeroFila, List<ErrorImportacion> errores) {
        if (celda == null || celda.getCellType() == CellType.BLANK) {
            return null;
        }
        if (celda.getCellType() == CellType.NUMERIC) {
            return new BigDecimal(((XSSFCell) celda).getRawValue());
        }
        if (celda.getCellType() == CellType.STRING) {
            try {
                return ConversorTextoImportacion.convertirImporte(celda.getStringCellValue());
            } catch (NumberFormatException ex) {
                errores.add(new ErrorImportacion(numeroFila, "importe", "Importe inválido"));
                return null;
            }
        }
        errores.add(new ErrorImportacion(numeroFila, "importe", "Importe inválido"));
        return null;
    }

    private String parsearTexto(
            Cell celda,
            int numeroFila,
            String campo,
            List<ErrorImportacion> errores) {
        if (celda == null || celda.getCellType() == CellType.BLANK) {
            return "";
        }
        if (celda.getCellType() == CellType.STRING) {
            return celda.getStringCellValue();
        }
        errores.add(new ErrorImportacion(numeroFila, campo, "Debe ser una celda de texto"));
        return null;
    }

    private boolean estaVacia(Row fila, Map<String, Integer> indices) {
        return indices.values().stream()
                .map(fila::getCell)
                .allMatch(celda -> celda == null
                        || celda.getCellType() == CellType.BLANK
                        || celda.getCellType() == CellType.STRING
                                && celda.getStringCellValue().isBlank());
    }
}
