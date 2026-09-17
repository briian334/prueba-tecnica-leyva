package com.leyva.pruebatecnica.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.leyva.pruebatecnica.api.dto.MovimientoResponse;
import com.leyva.pruebatecnica.api.dto.PolizaDetalleResponse;
import com.leyva.pruebatecnica.api.dto.PolizaResumenResponse;
import com.leyva.pruebatecnica.dominio.EstatusPoliza;
import com.leyva.pruebatecnica.poliza.servicio.PolizaConsultaService;
import com.leyva.pruebatecnica.poliza.servicio.PolizaNoEncontradaException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PolizaControllerTest {

    private PolizaConsultaService polizaConsultaService;
    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        polizaConsultaService = mock(PolizaConsultaService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PolizaController(polizaConsultaService))
                .setControllerAdvice(new ManejadorExcepcionesApi())
                .build();
    }

    @Test
    void listaResumenSinMovimientos() throws Exception {
        when(polizaConsultaService.listar()).thenReturn(List.of(new PolizaResumenResponse(
                15L,
                LocalDate.of(2026, 9, 16),
                "Importación de operaciones",
                "operaciones.xlsx",
                new BigDecimal("250.75"),
                new BigDecimal("250.75"),
                EstatusPoliza.GENERADA)));

        mockMvc.perform(get("/api/polizas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(15))
                .andExpect(jsonPath("$[0].fecha").value("2026-09-16"))
                .andExpect(jsonPath("$[0].archivoOrigen").value("operaciones.xlsx"))
                .andExpect(jsonPath("$[0].totalDebe").value(250.75))
                .andExpect(jsonPath("$[0].totalHaber").value(250.75))
                .andExpect(jsonPath("$[0].estatus").value("GENERADA"))
                .andExpect(jsonPath("$[0].movimientos").doesNotExist());
    }

    @Test
    void devuelveDetalleConMovimientosEnOrden() throws Exception {
        when(polizaConsultaService.obtenerDetalle(15L)).thenReturn(new PolizaDetalleResponse(
                15L,
                LocalDate.of(2026, 9, 16),
                "Importación de operaciones",
                "operaciones.xlsx",
                new BigDecimal("250.75"),
                new BigDecimal("250.75"),
                EstatusPoliza.GENERADA,
                LocalDateTime.of(2026, 9, 16, 18, 30, 45),
                List.of(
                        new MovimientoResponse(
                                31L, "1000-Caja", "REF-001", "Venta",
                                new BigDecimal("250.75"), new BigDecimal("0.00")),
                        new MovimientoResponse(
                                32L, "4100-Ventas", "REF-001", "Venta",
                                new BigDecimal("0.00"), new BigDecimal("250.75")))));

        mockMvc.perform(get("/api/polizas/15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value("2026-09-16"))
                .andExpect(jsonPath("$.fechaCreacion").value("2026-09-16T18:30:45"))
                .andExpect(jsonPath("$.movimientos[0].id").value(31))
                .andExpect(jsonPath("$.movimientos[0].cuenta").value("1000-Caja"))
                .andExpect(jsonPath("$.movimientos[0].debe").value(250.75))
                .andExpect(jsonPath("$.movimientos[1].id").value(32))
                .andExpect(jsonPath("$.movimientos[1].cuenta").value("4100-Ventas"))
                .andExpect(jsonPath("$.movimientos[1].haber").value(250.75));
    }

    @Test
    void devuelveErrorConsistenteCuandoLaPolizaNoExiste() throws Exception {
        when(polizaConsultaService.obtenerDetalle(99L))
                .thenThrow(new PolizaNoEncontradaException(99L));

        mockMvc.perform(get("/api/polizas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No se encontró la póliza con id 99"))
                .andExpect(jsonPath("$.errores").isEmpty());
    }

    @Test
    void conservaComoBadRequestUnIdentificadorConFormatoInvalido() throws Exception {
        mockMvc.perform(get("/api/polizas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La solicitud no es válida"))
                .andExpect(jsonPath("$.errores").isEmpty());
    }
}
