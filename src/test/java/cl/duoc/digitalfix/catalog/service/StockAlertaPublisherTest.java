package cl.duoc.digitalfix.catalog.service;

import cl.duoc.digitalfix.catalog.domain.Repuesto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Verifica que la alerta de stock se publica solo cuando el repuesto queda en
 * o bajo su minimo, sin necesidad de un broker real.
 */
class StockAlertaPublisherTest {

    private StockAlertaPublisher nuevoPublisher(RabbitTemplate template) {
        StockAlertaPublisher publisher = new StockAlertaPublisher(template, new ObjectMapper().findAndRegisterModules());
        ReflectionTestUtils.setField(publisher, "exchange", "catalogo.stock.exchange");
        ReflectionTestUtils.setField(publisher, "routingKey", "stock.bajo-minimo");
        return publisher;
    }

    private Repuesto repuesto(int stock, int stockMinimo) {
        return new Repuesto(7L, "SKU-1", "Cable", stock, stockMinimo, new BigDecimal("1000"));
    }

    @Test
    void publicaCuandoQuedaBajoMinimo() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        StockAlertaPublisher publisher = nuevoPublisher(template);

        publisher.publicarSiBajoMinimo(repuesto(2, 5));

        verify(template, times(1)).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void noPublicaCuandoHayStockSuficiente() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        StockAlertaPublisher publisher = nuevoPublisher(template);

        publisher.publicarSiBajoMinimo(repuesto(20, 5));

        verify(template, never()).convertAndSend(anyString(), anyString(), anyString());
    }
}
