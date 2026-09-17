package com.leyva.pruebatecnica.poliza.servicio;

public class PolizaNoEncontradaException extends RuntimeException {

    public PolizaNoEncontradaException(Long id) {
        super("No se encontró la póliza con id " + id);
    }
}
