package cl.duoc.digitalfix.catalog.service;

import cl.duoc.digitalfix.catalog.domain.Repuesto;
import cl.duoc.digitalfix.catalog.service.dto.AlertaStockDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Publica alertas de stock en el exchange direct cuando un repuesto queda
 * bajo su minimo.
 *
 * El envio es resiliente a proposito: si el broker no responde, se registra el
 * problema pero NO se cae la operacion de negocio (crear o actualizar el
 * repuesto). Una alerta perdida no debe revertir un guardado valido.
 */
@Service
public class StockAlertaPublisher {

    private static final Logger log = LoggerFactory.getLogger(StockAlertaPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${amqp.exchange.stock}")
    private String exchange;

    @Value("${amqp.routing-key.stock-bajo-minimo}")
    private String routingKey;

    public StockAlertaPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    /** Publica la alerta solo si el repuesto esta en o bajo su stock minimo. */
    public void publicarSiBajoMinimo(Repuesto repuesto) {
        if (!repuesto.estaBajoMinimo()) {
            return;
        }
        AlertaStockDto alerta = new AlertaStockDto(
                repuesto.getId(),
                repuesto.getCompanyId(),
                repuesto.getSku(),
                repuesto.getNombre(),
                repuesto.getStock(),
                repuesto.getStockMinimo(),
                Instant.now()
        );
        try {
            String json = objectMapper.writeValueAsString(alerta);
            rabbitTemplate.convertAndSend(exchange, routingKey, json);
            log.info("Alerta de stock publicada: repuesto {} (SKU {}) en {} de {} minimo.",
                    repuesto.getId(), repuesto.getSku(), repuesto.getStock(), repuesto.getStockMinimo());
        } catch (JsonProcessingException e) {
            log.error("No se pudo serializar la alerta de stock del repuesto {}.", repuesto.getId(), e);
        } catch (AmqpException e) {
            log.error("No se pudo publicar la alerta de stock del repuesto {}: {}",
                    repuesto.getId(), e.getMessage());
        }
    }
}
