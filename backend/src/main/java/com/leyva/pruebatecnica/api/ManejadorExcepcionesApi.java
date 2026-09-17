package com.leyva.pruebatecnica.api;

import com.leyva.pruebatecnica.api.dto.ErrorDetalleResponse;
import com.leyva.pruebatecnica.api.dto.ErrorResponse;
import com.leyva.pruebatecnica.importacion.servicio.ImportacionInvalidaException;
import com.leyva.pruebatecnica.poliza.servicio.PolizaNoEncontradaException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ManejadorExcepcionesApi extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManejadorExcepcionesApi.class);

    @ExceptionHandler(ImportacionInvalidaException.class)
    public ResponseEntity<ErrorResponse> manejarImportacionInvalida(
            ImportacionInvalidaException excepcion) {
        List<ErrorDetalleResponse> errores = excepcion.getErrores().stream()
                .map(error -> new ErrorDetalleResponse(
                        error.fila(), error.campo(), error.mensaje()))
                .toList();
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "El archivo contiene errores de validación",
                errores));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(
            MissingServletRequestPartException excepcion,
            HttpHeaders headers,
            HttpStatusCode estatus,
            WebRequest solicitud) {
        ErrorDetalleResponse detalle = new ErrorDetalleResponse(
                null, null, "El archivo es obligatorio");
        ErrorResponse respuesta = new ErrorResponse(
                "El archivo contiene errores de validación", List.of(detalle));
        return handleExceptionInternal(excepcion, respuesta, headers, estatus, solicitud);
    }

    @ExceptionHandler(PolizaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> manejarPolizaNoEncontrada(
            PolizaNoEncontradaException excepcion) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(excepcion.getMessage(), List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorInesperado(Exception excepcion) {
        LOGGER.error("Error inesperado al procesar la solicitud", excepcion);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Ocurrió un error inesperado", List.of()));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception excepcion,
            Object body,
            HttpHeaders headers,
            HttpStatusCode estatus,
            WebRequest solicitud) {
        ErrorResponse respuesta;
        if (body instanceof ErrorResponse errorResponse) {
            respuesta = errorResponse;
        } else if (estatus.is5xxServerError()) {
            LOGGER.error("Error inesperado al procesar la solicitud", excepcion);
            respuesta = new ErrorResponse("Ocurrió un error inesperado", List.of());
        } else {
            respuesta = new ErrorResponse("La solicitud no es válida", List.of());
        }
        return super.handleExceptionInternal(excepcion, respuesta, headers, estatus, solicitud);
    }
}
