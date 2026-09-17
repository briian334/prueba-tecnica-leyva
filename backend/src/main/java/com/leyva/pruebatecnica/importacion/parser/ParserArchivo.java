package com.leyva.pruebatecnica.importacion.parser;

import com.leyva.pruebatecnica.importacion.modelo.ResultadoParseo;
import java.io.IOException;
import java.io.InputStream;

public interface ParserArchivo {

    ResultadoParseo parsear(InputStream archivo) throws IOException;
}
