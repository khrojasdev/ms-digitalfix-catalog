package cl.duoc.digitalfix.catalog.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia de mensajeria del catalogo.
 *
 * catalog es un PRODUCTOR: cuando un repuesto queda bajo su stock minimo
 * publica una alerta de stock. Para esto usa un exchange de tipo DIRECT (a
 * diferencia del exchange de negocio, que es topic): la alerta viaja por una
 * routing key exacta hacia quien deba atenderla (el consumidor en notify).
 *
 * El nombre del exchange y la routing key se leen desde application.yml bajo
 * amqp.*, para no escribirlos a mano en el codigo de negocio.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${amqp.exchange.stock}")
    private String stockExchange;

    // Exchange DIRECT para alertas de stock.
    @Bean
    public DirectExchange stockExchange() {
        return new DirectExchange(stockExchange);
    }

    // RabbitAdmin declara el exchange al establecerse la conexion.
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
