CREATE TABLE polizas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    fecha DATE NOT NULL,
    concepto VARCHAR(255) NOT NULL,
    archivo_origen VARCHAR(255) NOT NULL,
    total_debe DECIMAL(19, 2) NOT NULL,
    total_haber DECIMAL(19, 2) NOT NULL,
    estatus VARCHAR(30) NOT NULL,
    fecha_creacion DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_polizas_total_debe_no_negativo CHECK (total_debe >= 0),
    CONSTRAINT chk_polizas_total_haber_no_negativo CHECK (total_haber >= 0)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;

CREATE TABLE movimientos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    poliza_id BIGINT NOT NULL,
    cuenta VARCHAR(100) NOT NULL,
    referencia VARCHAR(100) NOT NULL,
    concepto VARCHAR(255) NOT NULL,
    debe DECIMAL(19, 2) NOT NULL,
    haber DECIMAL(19, 2) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_movimientos_poliza_id (poliza_id),
    CONSTRAINT fk_movimientos_poliza
        FOREIGN KEY (poliza_id) REFERENCES polizas (id),
    CONSTRAINT chk_movimientos_debe_no_negativo CHECK (debe >= 0),
    CONSTRAINT chk_movimientos_haber_no_negativo CHECK (haber >= 0)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;
