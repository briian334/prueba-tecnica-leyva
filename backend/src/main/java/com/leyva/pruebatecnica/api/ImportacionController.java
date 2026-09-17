package com.leyva.pruebatecnica.api;

import com.leyva.pruebatecnica.api.dto.ImportacionResponse;
import com.leyva.pruebatecnica.importacion.modelo.ErrorImportacion;
import com.leyva.pruebatecnica.importacion.modelo.ImportacionResultado;
import com.leyva.pruebatecnica.importacion.servicio.ImportacionArchivoService;
import com.leyva.pruebatecnica.importacion.servicio.ImportacionInvalidaException;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/importaciones")
public class ImportacionController {

    private final ImportacionArchivoService importacionArchivoService;

    public ImportacionController(ImportacionArchivoService importacionArchivoService) {
        this.importacionArchivoService = importacionArchivoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportacionResponse> importar(
            @RequestPart("archivo") List<MultipartFile> archivos,
            MultipartHttpServletRequest solicitud) throws IOException {
        int cantidadArchivos = solicitud.getMultiFileMap().values().stream()
                .mapToInt(List::size)
                .sum();
        if (cantidadArchivos != 1) {
            throw new ImportacionInvalidaException(List.of(
                    new ErrorImportacion(
                            null, null, "Debe enviarse exactamente un archivo")));
        }
        ImportacionResultado resultado = importacionArchivoService.importar(archivos.getFirst());
        URI ubicacion = URI.create("/api/polizas/" + resultado.idPoliza());
        return ResponseEntity.created(ubicacion)
                .body(ImportacionResponse.desde(resultado));
    }
}
