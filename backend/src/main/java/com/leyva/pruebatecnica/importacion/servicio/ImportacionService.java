package com.leyva.pruebatecnica.importacion.servicio;

import com.leyva.pruebatecnica.dominio.Movimiento;
import com.leyva.pruebatecnica.dominio.Poliza;
import com.leyva.pruebatecnica.importacion.modelo.ErrorImportacion;
import com.leyva.pruebatecnica.importacion.modelo.RegistroImportado;
import com.leyva.pruebatecnica.importacion.modelo.ResultadoParseo;
import com.leyva.pruebatecnica.persistencia.PolizaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImportacionService {

    // Decisión temporal pendiente de confirmar en PEND-02.
    private static final String CONCEPTO_POLIZA_TEMPORAL = "Importación de operaciones";
    private static final BigDecimal CERO = new BigDecimal("0.00");

    private final PolizaRepository polizaRepository;

    public ImportacionService(PolizaRepository polizaRepository) {
        this.polizaRepository = polizaRepository;
    }

    /**
     * Genera y persiste una póliza a partir de registros previamente parseados.
     *
     * @param resultadoParseo registros normalizados y errores acumulados
     * @param archivoOrigen nombre original del archivo con extensión, sin ruta
     * @return póliza persistida con sus movimientos
     */
    @Transactional
    public Poliza generarYPersistir(ResultadoParseo resultadoParseo, String archivoOrigen) {
        validarErroresPrevios(resultadoParseo);

        List<RegistroImportado> registros = resultadoParseo.registros();
        LocalDate fecha = validarYObtenerFechaComun(registros);
        List<Movimiento> movimientos = generarMovimientos(registros);
        BigDecimal totalDebe = sumarDebe(movimientos);
        BigDecimal totalHaber = sumarHaber(movimientos);
        validarBalance(totalDebe, totalHaber);

        Poliza poliza = new Poliza(
                fecha,
                CONCEPTO_POLIZA_TEMPORAL,
                archivoOrigen,
                totalDebe,
                totalHaber,
                LocalDateTime.now());
        movimientos.forEach(poliza::agregarMovimiento);

        return polizaRepository.save(poliza);
    }

    private void validarErroresPrevios(ResultadoParseo resultadoParseo) {
        if (!resultadoParseo.errores().isEmpty()) {
            throw new ImportacionInvalidaException(resultadoParseo.errores());
        }
    }

    private LocalDate validarYObtenerFechaComun(List<RegistroImportado> registros) {
        if (registros.isEmpty()) {
            throw errorGlobal("El archivo no contiene operaciones");
        }

        LocalDate fecha = registros.get(0).fecha();
        boolean existenFechasDistintas = registros.stream()
                .map(RegistroImportado::fecha)
                .anyMatch(fechaRegistro -> !fecha.equals(fechaRegistro));
        if (existenFechasDistintas) {
            throw new ImportacionInvalidaException(List.of(
                    new ErrorImportacion(
                            null,
                            "fecha",
                            "Todas las operaciones deben tener la misma fecha")));
        }
        return fecha;
    }

    private List<Movimiento> generarMovimientos(List<RegistroImportado> registros) {
        List<Movimiento> movimientos = new ArrayList<>(registros.size() * 2);
        for (RegistroImportado registro : registros) {
            movimientos.add(new Movimiento(
                    registro.cuentaCargo(),
                    registro.referencia(),
                    registro.concepto(),
                    registro.importe(),
                    CERO));
            movimientos.add(new Movimiento(
                    registro.cuentaAbono(),
                    registro.referencia(),
                    registro.concepto(),
                    CERO,
                    registro.importe()));
        }
        return movimientos;
    }

    private BigDecimal sumarDebe(List<Movimiento> movimientos) {
        return movimientos.stream()
                .map(Movimiento::getDebe)
                .reduce(CERO, BigDecimal::add);
    }

    private BigDecimal sumarHaber(List<Movimiento> movimientos) {
        return movimientos.stream()
                .map(Movimiento::getHaber)
                .reduce(CERO, BigDecimal::add);
    }

    private void validarBalance(BigDecimal totalDebe, BigDecimal totalHaber) {
        if (totalDebe.compareTo(totalHaber) != 0) {
            throw new IllegalStateException("La póliza generada no está balanceada");
        }
    }

    private ImportacionInvalidaException errorGlobal(String mensaje) {
        return new ImportacionInvalidaException(List.of(new ErrorImportacion(null, null, mensaje)));
    }
}
