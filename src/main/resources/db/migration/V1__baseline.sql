-- Migracion inicial del esquema de catalogo (T-24).
--
-- Deliberadamente no crea tablas: deja el esquema bajo control de versiones y
-- fija el punto de partida. Las entidades llegan en las migraciones siguientes,
-- una por tarea del tablero, para que el historial de git muestre que trajo cada
-- una.

CREATE TABLE CATALOG_SCHEMA_INFO (
    CLAVE   VARCHAR2(50)  NOT NULL,
    VALOR   VARCHAR2(200) NOT NULL,
    CONSTRAINT PK_CATALOG_SCHEMA_INFO PRIMARY KEY (CLAVE)
);

INSERT INTO CATALOG_SCHEMA_INFO (CLAVE, VALOR)
VALUES ('servicio', 'ms-digitalfix-catalog');

INSERT INTO CATALOG_SCHEMA_INFO (CLAVE, VALOR)
VALUES ('dominio', 'servicios, repuestos y stock');
