# ms-digitalfix-catalog

**Proyecto:** DigitalFix - Gestion de ordenes de trabajo para una red de 20 empresas de mantenimiento electrico  
**Componente:** Microservicio  
**Asignatura:** DSY1107 Desarrollo Cloud Native I - Duoc UC  
**Primera entrega (EP1):** 14 de septiembre de 2026

## Descripcion

Catalogo tecnico: servicios, repuestos, stock y la relacion entre ambos.

## Tecnologias

Spring Boot 3, Spring Data JPA, Oracle, Flyway

## Mensajeria (RabbitMQ)

Nombres en `application.yml` (`amqp.*`). catalog es productor y consumidor:

| Rol | Exchange | Routing key | Cola / DLQ | Clase |
|---|---|---|---|---|
| Productor | `catalogo.stock.exchange` (direct) | `stock.bajo-minimo` | (la consume notify) | `service.StockAlertaPublisher`, beans en `config/RabbitMQConfig` |
| Consumidor | `ordenes.negocio.exchange` (topic) | `ordenes.eventos.asignada` | `catalogo.stock.ordenes.queue` / `catalogo.stock.ordenes.dlq` | `mensajeria.ordenes.OrdenAsignadaConsumer`, beans en `config/OrdenesRabbitConfig` |

Al asignar una orden se descuenta el stock de los repuestos de su servicio (todo o
nada, una vez por orden, en `ORDER_STOCK_USAGE`).

| Situacion | Decision (`mensajeria.ProcesadorDeMensajes`) |
|---|---|
| Descontado, sin repuestos o stock insuficiente (resultado de negocio, queda registrado) | `ACK` |
| Mensaje invalido (JSON ilegible o sin orden o empresa) | `NACK` sin reencolar: a la DLQ de inmediato |
| Falla transitoria (base de datos o red) | Hasta `amqp.reintentos.maximo` intentos (1 s, 2 s, 4 s...), luego a la DLQ |
| Otro error | `NACK` a la DLQ sin reintentar |

## Integrantes

| Integrante | GitHub |
|---|---|
| Kevin Rojas | @khrojasdev |
| Christopher Perez | @ChrisPerezV |
| Diego Lopez | @DiegoLopez-f |

## Repositorios del proyecto

- [`digitalfix-frontend`](https://github.com/khrojasdev/digitalfix-frontend) - Frontend
- [`ms-digitalfix-bff`](https://github.com/khrojasdev/ms-digitalfix-bff) - BFF / API interna
- [`ms-digitalfix-usuarios`](https://github.com/khrojasdev/ms-digitalfix-usuarios) - Microservicio
- [`ms-digitalfix-catalog`](https://github.com/khrojasdev/ms-digitalfix-catalog) - Microservicio **(este)**
- [`ms-digitalfix-workorders`](https://github.com/khrojasdev/ms-digitalfix-workorders) - Microservicio
- [`ms-digitalfix-notify`](https://github.com/khrojasdev/ms-digitalfix-notify) - Microservicio
- [`ms-digitalfix-report`](https://github.com/khrojasdev/ms-digitalfix-report) - Microservicio
- [`ms-digitalfix-audit`](https://github.com/khrojasdev/ms-digitalfix-audit) - Microservicio
- [`digitalfix-infra`](https://github.com/khrojasdev/digitalfix-infra) - Infraestructura


## Tablero

El backlog completo vive en un unico GitHub Project que enlaza los ocho repositorios.
Columnas: Backlog, To Do, In Progress, In Review, Done. Limite de trabajo en curso: 2 tarjetas por persona.

## Como se trabaja aqui

1. Cada tarea del tablero tiene su propia rama, indicada en el cuerpo del issue.
2. `git switch <rama>` - nunca se trabaja directamente sobre `main`.
3. Commits con Conventional Commits: `feat(catalogo): agrega endpoint de servicios`.
4. Pull request hacia `main` con `Closes #<numero del issue>` en la descripcion.
5. Revisa un companero distinto del autor. Recien ahi se hace merge.

Referencias entre repositorios: `khrojasdev/otro-repo#12`.

## Configuracion

Ninguna credencial vive en este repositorio. Todo llega por variables de entorno;
revisa `.env.example` para saber cuales.
