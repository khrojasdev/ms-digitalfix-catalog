package cl.duoc.digitalfix.catalog.mensajeria.ordenes;

import cl.duoc.digitalfix.catalog.mensajeria.ProcesadorDeMensajes;
import cl.duoc.digitalfix.catalog.service.ConsumoDeStockService;
import cl.duoc.digitalfix.catalog.service.dto.OrdenAsignadaEvento;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Dominio ordenes: consume ordenes.eventos.asignada y descuenta el stock.
 *
 * "Stock insuficiente" es una respuesta de negocio (queda registrada y se
 * confirma), no un error. ACK, reintento y DLQ los decide
 * {@link ProcesadorDeMensajes}: un evento sin orden o empresa va a la DLQ, y
 * si la base no responde se reintenta antes de mandarlo a la DLQ.
 */
@Component
public class OrdenAsignadaConsumer {

    private final ConsumoDeStockService consumo;
    private final ObjectMapper objectMapper;
    private final ProcesadorDeMensajes procesador;
    private final String cola;

    public OrdenAsignadaConsumer(ConsumoDeStockService consumo, ObjectMapper objectMapper,
                                 ProcesadorDeMensajes procesador, @Value("${amqp.queue.stock-ordenes}") String cola) {
        this.consumo = consumo;
        this.objectMapper = objectMapper;
        this.procesador = procesador;
        this.cola = cola;
    }

    @RabbitListener(queues = "${amqp.queue.stock-ordenes}", ackMode = "MANUAL")
    public void consumir(String mensajeJson, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        procesador.procesar(cola, channel, tag,
                () -> consumo.alAsignar(objectMapper.readValue(mensajeJson, OrdenAsignadaEvento.class)));
    }
}
