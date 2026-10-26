package cl.duoc.digitalfix.catalog.service;

import cl.duoc.digitalfix.catalog.service.dto.OrdenAsignadaEvento;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Consume ordenes.eventos.asignada y descuenta el stock.
 *
 * ACK manual cuando se proceso (tambien si el resultado fue "stock
 * insuficiente": es una respuesta de negocio, no un error). NACK sin reencolar
 * solo ante un error tecnico, para que el mensaje quede en la DLQ.
 */
@Service
public class OrdenAsignadaConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrdenAsignadaConsumer.class);

    private final ConsumoDeStockService consumo;
    private final ObjectMapper objectMapper;

    public OrdenAsignadaConsumer(ConsumoDeStockService consumo, ObjectMapper objectMapper) {
        this.consumo = consumo;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${amqp.queue.stock-ordenes}", ackMode = "MANUAL")
    public void consumir(String mensajeJson,
                         Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            OrdenAsignadaEvento evento = objectMapper.readValue(mensajeJson, OrdenAsignadaEvento.class);
            consumo.alAsignar(evento);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("No se pudo descontar stock por la orden asignada, se envia a la DLQ. Motivo: {}",
                    e.getMessage(), e);
            channel.basicNack(tag, false, false);
        }
    }
}
