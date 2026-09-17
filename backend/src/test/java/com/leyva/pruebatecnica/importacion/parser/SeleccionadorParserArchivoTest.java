package com.leyva.pruebatecnica.importacion.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SeleccionadorParserArchivoTest {

    @Test
    void seleccionaPorExtensionSinDistinguirMayusculas() {
        ParserArchivo txtParser = new TxtParser();
        ParserArchivo xlsxParser = new XlsxParser();
        SeleccionadorParserArchivo seleccionador =
                new SeleccionadorParserArchivo(txtParser, xlsxParser);

        assertThat(seleccionador.seleccionar("operaciones.TXT")).containsSame(txtParser);
        assertThat(seleccionador.seleccionar("operaciones.xlsx")).containsSame(xlsxParser);
        assertThat(seleccionador.seleccionar("operaciones.csv")).isEmpty();
        assertThat(seleccionador.seleccionar("sin-extension")).isEmpty();
    }
}
