package com.leyva.pruebatecnica.api.dto;

import com.leyva.pruebatecnica.dominio.EstatusPoliza;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PolizaDetalleResponse(
        Long id,
        LocalDate fecha,
        String concepto,
        String archivoOrigen,
        BigDecimal totalDebe,
        BigDecimal totalHaber,
        EstatusPoliza estatus,
        LocalDateTime fechaCreacion,
        List<MovimientoResponse> movimientos) {

    public PolizaDetalleResponse {
        movimientos = List.copyOf(movimientos);
    }
}
