package com.leyva.pruebatecnica.importacion.servicio;

import com.leyva.pruebatecnica.importacion.modelo.ErrorImportacion;
import java.util.List;

public class ImportacionInvalidaException extends RuntimeException {

    private final List<ErrorImportacion> errores;

    public ImportacionInvalidaException(List<ErrorImportacion> errores) {
        super("La importación contiene errores");
        this.errores = List.copyOf(errores);
    }

    public List<ErrorImportacion> getErrores() {
        return errores;
    }
}
