package com.leyva.pruebatecnica.importacion.parser;

import com.leyva.pruebatecnica.importacion.modelo.ErrorImportacion;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class EncabezadosImportacion {

    static final List<String> ESPERADOS = List.of(
            "fecha",
            "referencia",
            "concepto",
            "cuenta_cargo",
            "cuenta_abono",
            "importe");

    private EncabezadosImportacion() {
    }

    static ResultadoEncabezados resolver(List<String> encabezados) {
        Map<String, Integer> indices = new LinkedHashMap<>();
        Set<String> duplicados = new LinkedHashSet<>();
        Set<String> desconocidos = new LinkedHashSet<>();
        List<Integer> vacios = new ArrayList<>();

        for (int indice = 0; indice < encabezados.size(); indice++) {
            String encabezado = encabezados.get(indice);
            if (encabezado == null) {
                continue;
            }
            if (encabezado.isEmpty()) {
                vacios.add(indice);
                continue;
            }
            if (indices.putIfAbsent(encabezado, indice) != null) {
                duplicados.add(encabezado);
            }
            if (!ESPERADOS.contains(encabezado)) {
                desconocidos.add(encabezado);
            }
        }

        List<ErrorImportacion> errores = new ArrayList<>();
        ESPERADOS.stream()
                .filter(encabezado -> !indices.containsKey(encabezado))
                .map(encabezado -> errorGlobal("Falta el encabezado: " + encabezado))
                .forEach(errores::add);
        duplicados.stream()
                .map(encabezado -> errorGlobal("Encabezado duplicado: " + encabezado))
                .forEach(errores::add);
        desconocidos.stream()
                .map(encabezado -> errorGlobal("Encabezado desconocido: " + encabezado))
                .forEach(errores::add);
        vacios.stream()
                .map(indice -> errorGlobal("Encabezado vacío en la columna " + (indice + 1)))
                .forEach(errores::add);

        return new ResultadoEncabezados(Map.copyOf(indices), List.copyOf(errores));
    }

    private static ErrorImportacion errorGlobal(String mensaje) {
        return new ErrorImportacion(null, null, mensaje);
    }

    record ResultadoEncabezados(Map<String, Integer> indices, List<ErrorImportacion> errores) {
    }
}
