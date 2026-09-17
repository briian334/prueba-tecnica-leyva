package com.leyva.pruebatecnica.importacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistroImportado(
        LocalDate fecha,
        String referencia,
        String concepto,
        String cuentaCargo,
        String cuentaAbono,
        BigDecimal importe) {
}
