package com.leyva.pruebatecnica.importacion.parser;

import java.util.Locale;
import java.util.Optional;

public class SeleccionadorParserArchivo {

    private final ParserArchivo txtParser;
    private final ParserArchivo xlsxParser;

    public SeleccionadorParserArchivo(ParserArchivo txtParser, ParserArchivo xlsxParser) {
        this.txtParser = txtParser;
        this.xlsxParser = xlsxParser;
    }

    public Optional<ParserArchivo> seleccionar(String nombreArchivo) {
        if (nombreArchivo == null) {
            return Optional.empty();
        }
        int separadorExtension = nombreArchivo.lastIndexOf('.');
        if (separadorExtension < 0) {
            return Optional.empty();
        }
        String extension = nombreArchivo.substring(separadorExtension + 1).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case "txt" -> Optional.of(txtParser);
            case "xlsx" -> Optional.of(xlsxParser);
            default -> Optional.empty();
        };
    }
}
