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
4. validar estructura global;
5. normalizar filas recuperables;
6. acumular errores de parser;
7. validar reglas de dominio;
8. acumular errores de dominio;
9. abortar sin persistir si existe cualquier error;
10. construir póliza y movimientos en memoria;
11. calcular y validar balance;
12. iniciar transacción;
13. persistir agregado;
14. confirmar transacción;
15. devolver resultado.

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

La transacción comienza después del parseo y de todas las validaciones.

```text
parsear
→ validar
→ construir agregado
→ validar balance
→ @Transactional
→ persistir
```

Así no se mantiene una transacción abierta durante I/O del archivo.

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
