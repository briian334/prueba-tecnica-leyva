package com.leyva.pruebatecnica.api;

import com.leyva.pruebatecnica.api.dto.PolizaDetalleResponse;
import com.leyva.pruebatecnica.api.dto.PolizaResumenResponse;
import com.leyva.pruebatecnica.poliza.servicio.PolizaConsultaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/polizas")
public class PolizaController {

    private final PolizaConsultaService polizaConsultaService;

    public PolizaController(PolizaConsultaService polizaConsultaService) {
        this.polizaConsultaService = polizaConsultaService;
    }

    @GetMapping
    public List<PolizaResumenResponse> listar() {
        return polizaConsultaService.listar();
    }

    @GetMapping("/{id}")
    public PolizaDetalleResponse obtenerDetalle(@PathVariable Long id) {
        return polizaConsultaService.obtenerDetalle(id);
    }
}
