package com.leyva.pruebatecnica.api.dto;

import java.math.BigDecimal;

public record MovimientoResponse(
        Long id,
        String cuenta,
        String referencia,
        String concepto,
        BigDecimal debe,
        BigDecimal haber) {
}
