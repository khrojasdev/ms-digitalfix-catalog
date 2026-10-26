-- Descuento de stock al asignar una orden de trabajo.
-- Una fila por orden: la restriccion UK_OSU_ORDEN hace idempotente el consumo
-- del evento ASIGNADA (si RabbitMQ lo entrega dos veces, se descuenta una sola).

CREATE TABLE ORDER_STOCK_USAGE (
    ID          NUMBER GENERATED ALWAYS AS IDENTITY,
    COMPANY_ID  NUMBER         NOT NULL,
    ORDER_ID    NUMBER         NOT NULL,
    SERVICE_ID  NUMBER,
    EVENT_KEY   VARCHAR2(64)   NOT NULL,
    RESULTADO   VARCHAR2(30)   NOT NULL,
    DETALLE     VARCHAR2(500),
    CREADO_EN   TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_ORDER_STOCK_USAGE PRIMARY KEY (ID),
    CONSTRAINT UK_OSU_ORDEN UNIQUE (COMPANY_ID, ORDER_ID),
    CONSTRAINT CK_OSU_RESULTADO CHECK (RESULTADO IN
        ('DESCONTADO', 'STOCK_INSUFICIENTE', 'SIN_REPUESTOS', 'SERVICIO_NO_ENCONTRADO'))
);

COMMENT ON TABLE ORDER_STOCK_USAGE IS 'Resultado del descuento de stock de cada orden asignada';
