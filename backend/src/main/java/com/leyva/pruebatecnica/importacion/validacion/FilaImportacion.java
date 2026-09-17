package com.leyva.pruebatecnica.importacion.validacion;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FilaImportacion(
        int numero,
        LocalDate fecha,
        String referencia,
        String concepto,
        String cuentaCargo,
        String cuentaAbono,
        BigDecimal importe) {
}
