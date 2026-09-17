package com.leyva.pruebatecnica.api.dto;

import com.leyva.pruebatecnica.dominio.EstatusPoliza;
import com.leyva.pruebatecnica.importacion.modelo.ImportacionResultado;
import java.math.BigDecimal;

public record ImportacionResponse(
        Long idPoliza,
        int registrosProcesados,
        BigDecimal totalDebe,
        BigDecimal totalHaber,
        EstatusPoliza estatus) {

    public static ImportacionResponse desde(ImportacionResultado resultado) {
        return new ImportacionResponse(
                resultado.idPoliza(),
                resultado.registrosProcesados(),
                resultado.totalDebe(),
                resultado.totalHaber(),
                resultado.estatus());
    }
}
