package com.leyva.pruebatecnica.importacion.servicio;

import com.leyva.pruebatecnica.dominio.Poliza;
import com.leyva.pruebatecnica.importacion.modelo.ErrorImportacion;
import com.leyva.pruebatecnica.importacion.modelo.ImportacionResultado;
import com.leyva.pruebatecnica.importacion.modelo.ResultadoParseo;
import com.leyva.pruebatecnica.importacion.parser.ParserArchivo;
import com.leyva.pruebatecnica.importacion.parser.SeleccionadorParserArchivo;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImportacionArchivoService {

    private static final int LONGITUD_MAXIMA_NOMBRE_ARCHIVO = 255;

    private final SeleccionadorParserArchivo seleccionadorParserArchivo;
    private final ImportacionService importacionService;

    public ImportacionArchivoService(
            SeleccionadorParserArchivo seleccionadorParserArchivo,
            ImportacionService importacionService) {
        this.seleccionadorParserArchivo = seleccionadorParserArchivo;
        this.importacionService = importacionService;
    }

    public ImportacionResultado importar(MultipartFile archivo) throws IOException {
        validarArchivo(archivo);
        String nombreArchivo = normalizarNombre(archivo.getOriginalFilename());
        ParserArchivo parser = seleccionadorParserArchivo.seleccionar(nombreArchivo)
                .orElseThrow(() -> errorGlobal("La extensión del archivo no está soportada"));

        ResultadoParseo resultadoParseo;
        try (InputStream contenido = archivo.getInputStream()) {
            resultadoParseo = parser.parsear(contenido);
        }

        Poliza poliza = importacionService.generarYPersistir(resultadoParseo, nombreArchivo);
        return new ImportacionResultado(
                poliza.getId(),
                resultadoParseo.registros().size(),
                poliza.getTotalDebe(),
                poliza.getTotalHaber(),
                poliza.getEstatus());
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null) {
            throw errorGlobal("El archivo es obligatorio");
        }
        if (archivo.isEmpty()) {
            throw errorGlobal("El archivo está vacío");
        }
    }

    private String normalizarNombre(String nombreOriginal) {
        if (!StringUtils.hasText(nombreOriginal)) {
            throw errorGlobal("El archivo debe tener un nombre");
        }
        String nombreArchivo = StringUtils.getFilename(StringUtils.cleanPath(nombreOriginal));
        if (!StringUtils.hasText(nombreArchivo)) {
            throw errorGlobal("El archivo debe tener un nombre");
        }
        if (nombreArchivo.length() > LONGITUD_MAXIMA_NOMBRE_ARCHIVO) {
            throw errorGlobal("El nombre del archivo no puede exceder 255 caracteres");
        }
        return nombreArchivo;
    }

    private ImportacionInvalidaException errorGlobal(String mensaje) {
        return new ImportacionInvalidaException(List.of(
                new ErrorImportacion(null, null, mensaje)));
    }
}
