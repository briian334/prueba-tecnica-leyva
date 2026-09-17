package com.leyva.pruebatecnica.importacion.modelo;

import java.util.List;

public record ResultadoParseo(List<RegistroImportado> registros, List<ErrorImportacion> errores) {

    public ResultadoParseo {
        registros = List.copyOf(registros);
        errores = List.copyOf(errores);
    }
}
