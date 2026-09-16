# Contrato REST

## Convenciones

- base URL: `/api`;
- JSON en `camelCase`;
- `LocalDate` como `yyyy-MM-dd`;
- `LocalDateTime` en formato ISO;
- importes como números JSON;
- DTOs para entrada/salida;
- las entidades JPA no se exponen directamente.

## POST /api/importaciones

Importa un archivo y genera una póliza.

### Request

```text
Content-Type: multipart/form-data
```

Campo:

```text
archivo
```

Una solicitud contiene exactamente un archivo. El backend determina y valida si es TXT o XLSX.

### Éxito

```text
201 Created
Location: /api/polizas/{id}
```

```json
{
  "idPoliza": 1,
  "registrosProcesados": 25,
  "totalDebe": 15000.50,
  "totalHaber": 15000.50,
  "estatus": "GENERADA"
}
```

No incluye movimientos.

### Error de importación

```text
400 Bad Request
```

```json
{
  "mensaje": "El archivo contiene errores de validación",
  "errores": [
    {
      "fila": 4,
      "campo": "fecha",
      "mensaje": "Fecha inválida"
    }
  ]
}
```

Los errores globales pueden tener `fila` y `campo` en `null`.

## GET /api/polizas

Devuelve un listado resumido.

```text
200 OK
```

DTO:

```text
PolizaResumenResponse
- id
- fecha
- concepto
- archivoOrigen
- totalDebe
- totalHaber
- estatus
```

No incluye movimientos.

No se implementa paginación inicialmente porque la prueba no establece un volumen que la requiera. Si el volumen creciera, el endpoint puede evolucionar a Spring Data `Pageable`.

## GET /api/polizas/{id}

Devuelve la póliza con sus movimientos.

```text
200 OK
```

DTO:

```text
PolizaDetalleResponse
- id
- fecha
- concepto
- archivoOrigen
- totalDebe
- totalHaber
- estatus
- fechaCreacion
- movimientos
```

Movimiento:

```text
MovimientoResponse
- id
- cuenta
- referencia
- concepto
- debe
- haber
```

Los movimientos se devuelven en el orden lógico de generación/persistencia.

### Póliza inexistente

```text
404 Not Found
```

## Errores técnicos

Los fallos inesperados se responden como:

```text
500 Internal Server Error
```

No se utiliza un `catch(Exception)` genérico para convertirlos en `400`.

## Fuera de alcance inicial

- HATEOAS;
- wrappers genéricos;
- versionado `/v1`;
- filtros;
- ordenamiento configurable;
- paginación;
- Swagger/OpenAPI obligatorio;
- autenticación;
- autorización.
