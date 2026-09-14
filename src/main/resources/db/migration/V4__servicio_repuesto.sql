-- HU-18.1 - Relacion SERVICE_PART con clave compuesta.
-- La clave compuesta es lo que hace idempotente la asociacion: asociar dos veces
-- el mismo par actualiza la cantidad en vez de duplicar la fila.

CREATE TABLE SERVICE_PART (
    SERVICE_ID  NUMBER      NOT NULL,
    PART_ID     NUMBER      NOT NULL,
    CANTIDAD    NUMBER(6)   NOT NULL,
    CONSTRAINT PK_SERVICE_PART      PRIMARY KEY (SERVICE_ID, PART_ID),
    CONSTRAINT FK_SP_SERVICE        FOREIGN KEY (SERVICE_ID) REFERENCES SERVICE (ID),
    CONSTRAINT FK_SP_PART           FOREIGN KEY (PART_ID)    REFERENCES PART (ID),
    CONSTRAINT CK_SERVICE_PART_CANT CHECK (CANTIDAD > 0)
);

CREATE INDEX IDX_SERVICE_PART_PART ON SERVICE_PART (PART_ID);

COMMENT ON TABLE SERVICE_PART IS 'Cuantos repuestos de cada tipo consume un servicio';
