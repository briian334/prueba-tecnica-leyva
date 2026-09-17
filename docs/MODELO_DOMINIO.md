# Modelo de dominio

## Objetivo

Definir el modelo mínimo necesario para representar operaciones importadas, pólizas contables y movimientos, sin introducir entidades o reglas fuera del alcance de la prueba.

## RegistroImportado

`RegistroImportado` representa temporalmente una fila normalizada proveniente de TXT o XLSX. No es una entidad JPA y no se persiste.

```text
fecha: LocalDate
referencia: String
concepto: String
cuentaCargo: String
cuentaAbono: String
importe: BigDecimal
```

Reglas principales:

- `fecha` debe normalizarse de forma no ambigua a `LocalDate`.
- `referencia` es obligatoria y no puede quedar vacía después de `trim`.
- `concepto` se conserva como texto; no se impone una regla adicional de no vacío porque la especificación no la exige explícitamente.
- `cuentaCargo` y `cuentaAbono` son identificadores textuales obligatorios.
- `importe` debe ser mayor que cero y contener como máximo dos posiciones decimales significativas.
- Los importes se representan con `BigDecimal`.

## Fecha

TXT acepta únicamente texto `yyyy-MM-dd`.

XLSX acepta una celda de fecha nativa de Excel o texto `yyyy-MM-dd`. No se interpretan números genéricos como seriales de fecha cuando hacerlo requiera adivinar la intención.

Principio:

```text
si puede normalizarse sin adivinar → aceptar
si requiere inferencia ambigua → rechazar
```

## Referencia

Se trata como `String`, se aplica `trim`, es obligatoria y no se exige unicidad.

## Concepto

Se trata como `String`, se conserva y se aplica `trim`. No se añade una validación de contenido no solicitada.

## Cuentas

`cuentaCargo` y `cuentaAbono` se modelan como `String`.

Ejemplo válido:

```text
1100-Clientes
```

No se aplica un patrón numérico ni validación contra catálogo porque la prueba no incluye uno.

## Importe

Tipo Java:

```text
BigDecimal
```

Reglas:

- obligatorio;
- mayor que cero;
- máximo dos decimales significativos;
- separador decimal lógico: punto.

Ejemplos:

```text
100      → 100.00
100.5    → 100.50
100.500  → 100.50
100.501  → error
```

En TXT se rechazan valores como `1,250.50`, `1250,50` o `$1250.50`.

En XLSX, una celda numérica con valor `1250.50` sigue siendo válida aunque Excel la muestre con separador de miles.

## Poliza

Entidad persistente y raíz del agregado contable.

```text
id: Long
fecha: LocalDate
concepto: String
archivoOrigen: String
totalDebe: BigDecimal
totalHaber: BigDecimal
estatus: EstatusPoliza
fechaCreacion: LocalDateTime
movimientos: List<Movimiento>
```

Reglas:

- una importación válida genera exactamente una póliza;
- los totales se calculan a partir de los movimientos;
- `totalDebe` y `totalHaber` deben coincidir;
- `archivoOrigen` almacena nombre y extensión, no ruta ni binario;
- la póliza administra sus movimientos como agregado.

## Movimiento

Entidad persistente asociada a una póliza.

```text
id: Long
poliza: Poliza
cuenta: String
referencia: String
concepto: String
debe: BigDecimal
haber: BigDecimal
```

Cada `RegistroImportado` genera exactamente dos movimientos:

```text
Cargo:
cuenta = cuentaCargo
debe = importe
haber = 0.00

Abono:
cuenta = cuentaAbono
debe = 0.00
haber = importe
```

Ambos conservan `referencia` y `concepto`.

## Estatus

Estado inicial:

```text
GENERADA
```

Una importación inválida no crea póliza, por lo que no se necesita un estado `ERROR`.

## Invariantes

- cada operación fuente genera dos movimientos;
- un movimiento tiene exactamente un lado contable con importe positivo;
- los importes no son negativos;
- `totalDebe == totalHaber`;
- un movimiento siempre pertenece a una póliza.

## Pendientes funcionales

### PEND-01 — Fecha de póliza

Debe confirmarse cómo se determina `Poliza.fecha`, especialmente si un archivo contiene operaciones con fechas distintas.

Supuesto temporal pendiente de confirmación:

- todos los `RegistroImportado` deben tener la misma fecha;
- una importación con fechas distintas se rechaza sin persistir;
- la fecha común se utiliza como `Poliza.fecha`.

Este supuesto está aislado en la validación de fecha común del servicio de importación para facilitar su cambio cuando Leyva confirme la regla definitiva.

### PEND-02 — Concepto general de póliza

Debe confirmarse cómo se obtiene `Poliza.concepto`, ya que el archivo sólo define concepto por operación.

Supuesto temporal pendiente de confirmación:

```text
Importación de operaciones
```

El valor no se deriva de los conceptos individuales ni modifica el contrato REST. Se mantiene como una constante temporal identificada en el servicio de importación para facilitar su sustitución cuando Leyva confirme la regla definitiva.
