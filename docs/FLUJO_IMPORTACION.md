# Flujo de importación y validaciones

## Objetivo

Procesar archivos TXT y XLSX con una estrategia común de validación, generación contable y persistencia atómica.

## Flujo general

```text
Archivo
→ validación global
→ parser específico
→ normalización
→ validación de dominio
→ generación de póliza y movimientos
→ validación de balance
→ persistencia transaccional
→ respuesta
```

Secuencia:

1. recibir archivo;
2. validar existencia y formato soportado;
3. seleccionar parser;
4. leer y parsear el archivo fuera de una transacción;
5. validar estructura global;
6. normalizar filas recuperables;
7. validar reglas por fila y acumular errores;
8. invocar `ImportacionService.generarYPersistir()` e iniciar la transacción;
9. abortar sin persistir si existen errores previos;
10. validar que todas las operaciones compartan la misma fecha;
11. construir la póliza y sus movimientos en memoria;
12. calcular y validar el balance;
13. persistir el agregado;
14. confirmar la transacción;
15. devolver el resultado.

## Parsers

```text
ParserArchivo
├── TxtParser
└── XlsxParser
```

Ambos producen `RegistroImportado`, evitando duplicar validaciones y generación contable.

## ResultadoParseo

```text
ResultadoParseo
- List<RegistroImportado> registros
- List<ErrorImportacion> errores
```

No se crean registros parcialmente válidos.

## ErrorImportacion

```text
fila: Integer?
campo: String?
mensaje: String
```

`fila` y `campo` pueden ser nulos para errores globales.

## Errores globales

Detienen el procesamiento inmediatamente:

- archivo vacío;
- extensión no soportada;
- XLSX corrupto;
- TXT ilegible;
- ausencia de encabezados;
- encabezados incompatibles;
- hoja requerida inexistente.

## Errores por fila

Se acumulan cuando es posible continuar:

- número incorrecto de campos;
- fecha inválida;
- importe no interpretable;
- referencia vacía;
- cuenta vacía;
- importe menor o igual a cero;
- precisión decimal inválida.

## Responsabilidades

El parser responde:

```text
¿Puede convertirse técnica y unívocamente al tipo esperado?
```

La validación de dominio responde:

```text
¿El valor normalizado cumple las reglas funcionales?
```

Ejemplos de parser: fecha no interpretable, importe con formato inválido, tipo de celda ambiguo.

Ejemplos de dominio: referencia vacía, cuenta vacía, importe <= 0, precisión inválida.

## TXT

- UTF-8;
- separador `|`;
- encabezados en primera fila;
- sin dependencia de quoting;
- número exacto de campos.

## XLSX

- una hoja de datos;
- encabezados en primera fila;
- detectar encabezados faltantes;
- detectar duplicados;
- detectar encabezados desconocidos.

## All-or-nothing

Si existe cualquier error:

```text
0 pólizas persistidas
0 movimientos persistidos
```

No se persisten filas válidas de un archivo parcialmente inválido.

## Generación contable

```text
1 RegistroImportado
→ 2 Movimiento
```

Cargo:

```text
debe = importe
haber = 0.00
```

Abono:

```text
debe = 0.00
haber = importe
```

## Balance

Antes de persistir:

```text
totalDebe == totalHaber
```

Una diferencia indicaría una inconsistencia interna.

## Transacción

La lectura y el parsing del archivo ocurren antes de abrir la transacción, por lo que el I/O del archivo no mantiene recursos transaccionales ocupados.

La transacción comienza al invocar el método `ImportacionService.generarYPersistir()`. Dentro de ella se comprueban los errores acumulados, se valida la fecha común, se generan la póliza y sus movimientos, se valida el balance y se persiste el agregado.

```text
fuera de la transacción:
leer archivo → parsear → normalizar y validar filas

dentro de ImportacionService.generarYPersistir() (@Transactional):
comprobar errores previos
→ validar fecha común
→ construir póliza y movimientos
→ validar balance
→ persistir agregado
```

## Error controlado

Los errores esperados se representan mediante una excepción funcional equivalente a:

```text
ImportacionInvalidaException
```

con:

```text
List<ErrorImportacion>
```

Los errores técnicos inesperados no se convierten artificialmente en validaciones.

## Resultado exitoso

```text
ImportacionResultado
- idPoliza
- registrosProcesados
- totalDebe
- totalHaber
- estatus
```

No se requiere un booleano `exito`.

## Riesgo aceptado

Una fila que no puede normalizarse no pasa a validación de dominio, por lo que puede no reportar todos sus errores posibles en una sola ejecución. La especificación no exige diagnóstico exhaustivo por fila.
