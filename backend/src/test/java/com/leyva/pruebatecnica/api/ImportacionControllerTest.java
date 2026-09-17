package com.leyva.pruebatecnica.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.leyva.pruebatecnica.dominio.EstatusPoliza;
import com.leyva.pruebatecnica.importacion.modelo.ErrorImportacion;
import com.leyva.pruebatecnica.importacion.modelo.ImportacionResultado;
import com.leyva.pruebatecnica.importacion.servicio.ImportacionArchivoService;
import com.leyva.pruebatecnica.importacion.servicio.ImportacionInvalidaException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ImportacionControllerTest {

    private ImportacionArchivoService importacionArchivoService;
    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        importacionArchivoService = mock(ImportacionArchivoService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ImportacionController(importacionArchivoService))
                .setControllerAdvice(new ManejadorExcepcionesApi())
                .build();
    }

    @Test
    void creaLaPolizaYDevuelveLocationYResumen() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "operaciones.txt", "text/plain", "contenido".getBytes());
        when(importacionArchivoService.importar(any())).thenReturn(new ImportacionResultado(
                21L,
                3,
                new BigDecimal("15000.50"),
                new BigDecimal("15000.50"),
                EstatusPoliza.GENERADA));

        mockMvc.perform(multipart("/api/importaciones").file(archivo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/polizas/21"))
                .andExpect(jsonPath("$.idPoliza").value(21))
                .andExpect(jsonPath("$.registrosProcesados").value(3))
                .andExpect(jsonPath("$.totalDebe").value(15000.50))
                .andExpect(jsonPath("$.totalHaber").value(15000.50))
                .andExpect(jsonPath("$.estatus").value("GENERADA"));
    }

    @Test
    void devuelveErroresDeImportacionConEstructuraConsistente() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "operaciones.txt", "text/plain", "contenido".getBytes());
        when(importacionArchivoService.importar(any())).thenThrow(
                new ImportacionInvalidaException(List.of(
                        new ErrorImportacion(4, "fecha", "Fecha inválida"))));

        mockMvc.perform(multipart("/api/importaciones").file(archivo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El archivo contiene errores de validación"))
                .andExpect(jsonPath("$.errores[0].fila").value(4))
                .andExpect(jsonPath("$.errores[0].campo").value("fecha"))
                .andExpect(jsonPath("$.errores[0].mensaje").value("Fecha inválida"));
    }

    @Test
    void devuelveErrorControladoCuandoFaltaElCampoArchivo() throws Exception {
        mockMvc.perform(multipart("/api/importaciones"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El archivo contiene errores de validación"))
                .andExpect(jsonPath("$.errores[0].fila").isEmpty())
                .andExpect(jsonPath("$.errores[0].campo").isEmpty())
                .andExpect(jsonPath("$.errores[0].mensaje").value("El archivo es obligatorio"));
    }

    @Test
    void rechazaMasDeUnArchivo() throws Exception {
        MockMultipartFile primerArchivo = new MockMultipartFile(
                "archivo", "uno.txt", "text/plain", "contenido".getBytes());
        MockMultipartFile segundoArchivo = new MockMultipartFile(
                "archivo", "dos.txt", "text/plain", "contenido".getBytes());

        mockMvc.perform(multipart("/api/importaciones")
                        .file(primerArchivo)
                        .file(segundoArchivo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El archivo contiene errores de validación"))
                .andExpect(jsonPath("$.errores[0].fila").isEmpty())
                .andExpect(jsonPath("$.errores[0].campo").isEmpty())
                .andExpect(jsonPath("$.errores[0].mensaje")
                        .value("Debe enviarse exactamente un archivo"));
    }

    @Test
    void rechazaUnArchivoAdicionalEnOtraParte() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "operaciones.txt", "text/plain", "contenido".getBytes());
        MockMultipartFile archivoAdicional = new MockMultipartFile(
                "otro", "adicional.txt", "text/plain", "contenido".getBytes());

        mockMvc.perform(multipart("/api/importaciones")
                        .file(archivo)
                        .file(archivoAdicional))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El archivo contiene errores de validación"))
                .andExpect(jsonPath("$.errores[0].mensaje")
                        .value("Debe enviarse exactamente un archivo"));
    }

    @Test
    void ocultaDetallesTecnicosEnErroresInesperados() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "operaciones.txt", "text/plain", "contenido".getBytes());
        when(importacionArchivoService.importar(any()))
                .thenThrow(new IllegalStateException("detalle técnico sensible"));

        String respuesta = mockMvc.perform(multipart("/api/importaciones").file(archivo))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Ocurrió un error inesperado"))
                .andExpect(jsonPath("$.errores").isEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(respuesta).doesNotContain("detalle técnico sensible", "IllegalStateException");
    }
}
