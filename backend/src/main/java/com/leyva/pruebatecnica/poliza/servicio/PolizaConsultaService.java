package com.leyva.pruebatecnica.poliza.servicio;

import com.leyva.pruebatecnica.api.dto.MovimientoResponse;
import com.leyva.pruebatecnica.api.dto.PolizaDetalleResponse;
import com.leyva.pruebatecnica.api.dto.PolizaResumenResponse;
import com.leyva.pruebatecnica.dominio.Movimiento;
import com.leyva.pruebatecnica.dominio.Poliza;
import com.leyva.pruebatecnica.persistencia.PolizaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PolizaConsultaService {

    private final PolizaRepository polizaRepository;

    public PolizaConsultaService(PolizaRepository polizaRepository) {
        this.polizaRepository = polizaRepository;
    }

    public List<PolizaResumenResponse> listar() {
        return polizaRepository.findAll().stream()
                .map(this::crearResumen)
                .toList();
    }

    public PolizaDetalleResponse obtenerDetalle(Long id) {
        Poliza poliza = polizaRepository.buscarDetallePorId(id)
                .orElseThrow(() -> new PolizaNoEncontradaException(id));
        return crearDetalle(poliza);
    }

    private PolizaResumenResponse crearResumen(Poliza poliza) {
        return new PolizaResumenResponse(
                poliza.getId(),
                poliza.getFecha(),
                poliza.getConcepto(),
                poliza.getArchivoOrigen(),
                poliza.getTotalDebe(),
                poliza.getTotalHaber(),
                poliza.getEstatus());
    }

    private PolizaDetalleResponse crearDetalle(Poliza poliza) {
        List<MovimientoResponse> movimientos = poliza.getMovimientos().stream()
                .map(this::crearMovimiento)
                .toList();
        return new PolizaDetalleResponse(
                poliza.getId(),
                poliza.getFecha(),
                poliza.getConcepto(),
                poliza.getArchivoOrigen(),
                poliza.getTotalDebe(),
                poliza.getTotalHaber(),
                poliza.getEstatus(),
                poliza.getFechaCreacion(),
                movimientos);
    }

    private MovimientoResponse crearMovimiento(Movimiento movimiento) {
        return new MovimientoResponse(
                movimiento.getId(),
                movimiento.getCuenta(),
                movimiento.getReferencia(),
                movimiento.getConcepto(),
                movimiento.getDebe(),
                movimiento.getHaber());
    }
}
