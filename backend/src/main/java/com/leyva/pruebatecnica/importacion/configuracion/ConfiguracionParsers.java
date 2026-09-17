package com.leyva.pruebatecnica.importacion.configuracion;

import com.leyva.pruebatecnica.importacion.parser.SeleccionadorParserArchivo;
import com.leyva.pruebatecnica.importacion.parser.TxtParser;
import com.leyva.pruebatecnica.importacion.parser.XlsxParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionParsers {

    @Bean
    SeleccionadorParserArchivo seleccionadorParserArchivo() {
        return new SeleccionadorParserArchivo(new TxtParser(), new XlsxParser());
    }
}
