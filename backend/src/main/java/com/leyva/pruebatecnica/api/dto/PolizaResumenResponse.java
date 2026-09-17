package com.leyva.pruebatecnica.api.dto;

import com.leyva.pruebatecnica.dominio.EstatusPoliza;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PolizaResumenResponse(
        Long id,
        LocalDate fecha,
        String concepto,
        String archivoOrigen,
        BigDecimal totalDebe,
        BigDecimal totalHaber,
        EstatusPoliza estatus) {
}
