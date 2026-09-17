package com.leyva.pruebatecnica.importacion.validacion;

import com.leyva.pruebatecnica.importacion.modelo.ErrorImportacion;
import com.leyva.pruebatecnica.importacion.modelo.RegistroImportado;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ValidadorRegistroImportado {

    public ValidadorRegistroImportado() {
    }

    public ResultadoValidacion validar(FilaImportacion fila) {
        List<ErrorImportacion> errores = new ArrayList<>();
        String referencia = normalizar(fila.referencia());
        String concepto = normalizar(fila.concepto());
        String cuentaCargo = normalizar(fila.cuentaCargo());
        String cuentaAbono = normalizar(fila.cuentaAbono());

        if (fila.fecha() == null) {
            errores.add(error(fila, "fecha", "La fecha es obligatoria"));
        }
        if (referencia.isEmpty()) {
            errores.add(error(fila, "referencia", "La referencia es obligatoria"));
        }
        if (cuentaCargo.isEmpty()) {
            errores.add(error(fila, "cuenta_cargo", "La cuenta de cargo es obligatoria"));
        }
        if (cuentaAbono.isEmpty()) {
            errores.add(error(fila, "cuenta_abono", "La cuenta de abono es obligatoria"));
        }

        BigDecimal importeNormalizado = validarImporte(fila, errores);
        if (!errores.isEmpty()) {
            return new ResultadoValidacion(null, errores);
        }

        RegistroImportado registro = new RegistroImportado(
                fila.fecha(),
                referencia,
                concepto,
                cuentaCargo,
                cuentaAbono,
                importeNormalizado);
        return new ResultadoValidacion(registro, List.of());
    }

    private BigDecimal validarImporte(FilaImportacion fila, List<ErrorImportacion> errores) {
        BigDecimal importe = fila.importe();
        if (importe == null) {
            errores.add(error(fila, "importe", "El importe es obligatorio"));
            return null;
        }
        if (importe.signum() <= 0) {
            errores.add(error(fila, "importe", "El importe debe ser mayor que cero"));
        }
        if (importe.stripTrailingZeros().scale() > 2) {
            errores.add(error(fila, "importe", "El importe admite como máximo dos decimales"));
        }
        return importe.stripTrailingZeros().scale() <= 2 ? importe.setScale(2) : null;
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private ErrorImportacion error(FilaImportacion fila, String campo, String mensaje) {
        return new ErrorImportacion(fila.numero(), campo, mensaje);
    }

    public record ResultadoValidacion(
            RegistroImportado registro, List<ErrorImportacion> errores) {

        public ResultadoValidacion {
            errores = List.copyOf(errores);
        }
    }
}
