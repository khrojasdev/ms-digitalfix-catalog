package cl.duoc.digitalfix.catalog.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * catalog tambien es CONSUMIDOR: escucha las ordenes ASIGNADAS para descontar
 * el stock de los repuestos del servicio.
 *
 * La cola se enlaza al exchange topic de negocio solo con la routing key
 * ordenes.eventos.asignada (no recibe las demas). Si el mensaje falla, el NACK
 * sin reencolar lo manda a la DLQ por el exchange por defecto, igual que en
 * audit, report y notify.
 */
@Configuration
public class OrdenesRabbitConfig {

    @Value("${amqp.exchange.negocio}")
    private String negocioExchangeName;

    @Value("${amqp.queue.stock-ordenes}")
    private String colaName;

    @Value("${amqp.queue.stock-ordenes-dlq}")
    private String dlqName;

    @Value("${amqp.routing-key.orden-asignada}")
    private String routingKey;

    /** Mismas propiedades con que lo declara workorders (durable, sin auto-borrado). */
    @Bean
    public TopicExchange negocioExchange() {
        return new TopicExchange(negocioExchangeName, true, false);
    }

    @Bean
    public Queue stockOrdenesDlq() {
        return QueueBuilder.durable(dlqName).build();
    }

    @Bean
    public Queue stockOrdenesQueue() {
        return QueueBuilder.durable(colaName)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", dlqName)
                .build();
    }

    @Bean
    public Binding stockOrdenesBinding(Queue stockOrdenesQueue, TopicExchange negocioExchange) {
        return BindingBuilder.bind(stockOrdenesQueue).to(negocioExchange).with(routingKey);
    }
}
