# Persistencia MySQL y JPA

## Objetivo

Definir un esquema relacional mínimo, reproducible y consistente con el modelo de dominio.

## Convenciones

Entidades Java:

```text
Poliza
Movimiento
```

Tablas:

```text
polizas
movimientos
```

FK:

```text
movimientos.poliza_id
```

## Esquema

### polizas

```text
id              BIGINT AUTO_INCREMENT PRIMARY KEY
fecha           DATE NOT NULL
concepto        VARCHAR(255) NOT NULL
archivo_origen  VARCHAR(255) NOT NULL
total_debe      DECIMAL(19,2) NOT NULL
total_haber     DECIMAL(19,2) NOT NULL
estatus         VARCHAR(30) NOT NULL
fecha_creacion  DATETIME(3) NOT NULL
```

### movimientos

```text
id              BIGINT AUTO_INCREMENT PRIMARY KEY
poliza_id       BIGINT NOT NULL
cuenta          VARCHAR(100) NOT NULL
referencia      VARCHAR(100) NOT NULL
concepto        VARCHAR(255) NOT NULL
debe            DECIMAL(19,2) NOT NULL
haber           DECIMAL(19,2) NOT NULL
```

## IDs

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

correspondiente a `BIGINT AUTO_INCREMENT`.

## Charset

Se utiliza `utf8mb4`.

## Importes

SQL:

```text
DECIMAL(19,2)
```

Java:

```text
BigDecimal
```

## Fechas

```text
fecha          DATE        ↔ LocalDate
fecha_creacion DATETIME(3) ↔ LocalDateTime
```

## Estatus

```java
@Enumerated(EnumType.STRING)
```

SQL:

```text
VARCHAR(30)
```

Estado inicial: `GENERADA`.

## Foreign key

```sql
FOREIGN KEY (poliza_id)
REFERENCES polizas(id)
```

Sin `ON DELETE CASCADE` ni `ON DELETE SET NULL`.

## Índices

```text
polizas
- PRIMARY KEY (id)

movimientos
- PRIMARY KEY (id)
- INDEX idx_movimientos_poliza_id (poliza_id)
```

No se agregan índices preventivos sobre campos sin consultas requeridas.

## CHECK

```text
total_debe >= 0
total_haber >= 0
debe >= 0
haber >= 0
```

Las invariantes contables completas permanecen en Java.

## Relación JPA

```text
Poliza 1 ───── N Movimiento
```

`Movimiento` es el lado propietario.

```java
@OneToMany(
    mappedBy = "poliza",
    fetch = FetchType.LAZY,
    cascade = CascadeType.PERSIST
)
```

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "poliza_id", nullable = false)
```

`Poliza` centraliza la asociación mediante una operación equivalente a `agregarMovimiento`.

## Cascade

Sólo:

```text
CascadeType.PERSIST
```

No se usa `CascadeType.ALL` ni `orphanRemoval=true`.

## Fetch

```text
Poliza.movimientos → LAZY
Movimiento.poliza  → LAZY
```

## Detalle

`GET /api/polizas/{id}` utilizará una consulta específica con `JOIN FETCH`.

```text
Listado → consulta ligera
Detalle → póliza + movimientos
```

## Migraciones

Se utiliza Flyway:

```text
backend/src/main/resources/db/migration/
└── V1__crear_esquema_inicial.sql
```

Flyway crea/evoluciona el esquema y Hibernate lo valida con:

```text
ddl-auto=validate
```

No se usa `ddl-auto=create` ni `ddl-auto=update` como mecanismo de entrega.

## DDL conceptual

```sql
CREATE TABLE polizas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    concepto VARCHAR(255) NOT NULL,
    archivo_origen VARCHAR(255) NOT NULL,
    total_debe DECIMAL(19,2) NOT NULL,
    total_haber DECIMAL(19,2) NOT NULL,
    estatus VARCHAR(30) NOT NULL,
    fecha_creacion DATETIME(3) NOT NULL,
    CHECK (total_debe >= 0),
    CHECK (total_haber >= 0)
);

CREATE TABLE movimientos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    poliza_id BIGINT NOT NULL,
    cuenta VARCHAR(100) NOT NULL,
    referencia VARCHAR(100) NOT NULL,
    concepto VARCHAR(255) NOT NULL,
    debe DECIMAL(19,2) NOT NULL,
    haber DECIMAL(19,2) NOT NULL,
    CONSTRAINT fk_movimientos_poliza
        FOREIGN KEY (poliza_id)
        REFERENCES polizas(id),
    CHECK (debe >= 0),
    CHECK (haber >= 0),
    INDEX idx_movimientos_poliza_id (poliza_id)
);
```

## Decisiones fuera de alcance

- soft delete;
- `ON DELETE CASCADE`;
- auditoría avanzada;
- `@Version`;
- caché;
- triggers;
- stored procedures;
- UUID;
- índices para filtros inexistentes;
- generación de esquema mediante Hibernate;
- Lombok.

## Evolución

- paginación → `Pageable`;
- filtros → índices basados en consultas reales;
- concurrencia → `@Version` si aparece necesidad real;
- eliminación → definir primero semántica física/lógica/contable.

## Pendientes funcionales

```text
PEND-01: origen de Poliza.fecha
PEND-02: origen de Poliza.concepto
```

Afectan los valores almacenados, no el esquema.
