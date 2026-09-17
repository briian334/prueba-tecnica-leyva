package com.leyva.pruebatecnica.importacion.parser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

final class ConversorTextoImportacion {

    private static final Pattern FECHA = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern IMPORTE = Pattern.compile("-?\\d+(?:\\.\\d+)?");

    private ConversorTextoImportacion() {
    }

    static LocalDate convertirFecha(String texto) {
        String valor = texto.trim();
        if (valor.isEmpty()) {
            return null;
        }
        if (!FECHA.matcher(valor).matches()) {
            throw new DateTimeParseException("Formato de fecha inválido", valor, 0);
        }
        return LocalDate.parse(valor);
    }

    static BigDecimal convertirImporte(String texto) {
        String valor = texto.trim();
        if (valor.isEmpty()) {
            return null;
        }
        if (!IMPORTE.matcher(valor).matches()) {
            throw new NumberFormatException("Formato de importe inválido");
        }
        return new BigDecimal(valor);
    }
}
