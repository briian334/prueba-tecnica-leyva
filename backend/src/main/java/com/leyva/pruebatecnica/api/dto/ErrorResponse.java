package com.leyva.pruebatecnica.api.dto;

import java.util.List;

public record ErrorResponse(String mensaje, List<ErrorDetalleResponse> errores) {

    public ErrorResponse {
        errores = List.copyOf(errores);
    }
}
