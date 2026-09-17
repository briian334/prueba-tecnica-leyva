package com.leyva.pruebatecnica.importacion.modelo;

import com.leyva.pruebatecnica.dominio.EstatusPoliza;
import java.math.BigDecimal;

public record ImportacionResultado(
        Long idPoliza,
        int registrosProcesados,
        BigDecimal totalDebe,
        BigDecimal totalHaber,
        EstatusPoliza estatus) {
}
