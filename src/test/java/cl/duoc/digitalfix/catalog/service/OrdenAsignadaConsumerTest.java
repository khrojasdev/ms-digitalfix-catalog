package cl.duoc.digitalfix.catalog.service;

import cl.duoc.digitalfix.catalog.service.dto.OrdenAsignadaEvento;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** ACK manual al procesar y NACK sin reencolar (DLQ) ante error. */
class OrdenAsignadaConsumerTest {

    private final ConsumoDeStockService consumo = mock(ConsumoDeStockService.class);
    private final Channel canal = mock(Channel.class);
    private final OrdenAsignadaConsumer consumidor =
            new OrdenAsignadaConsumer(consumo, new ObjectMapper().findAndRegisterModules());

    @Test
    void confirmaYLeeSoloLoQueNecesita() throws Exception {
        String json = "{\"eventKey\":\"k1\",\"tipoEvento\":\"ASIGNADA\",\"ordenId\":5,\"empresaId\":1,"
                + "\"servicioId\":3,\"estadoAnterior\":\"CREADA\",\"tecnicoNombre\":\"Pedro\",\"fecha\":\"2026-10-26T10:00:00\"}";

        consumidor.consumir(json, canal, 7L);

        ArgumentCaptor<OrdenAsignadaEvento> evento = ArgumentCaptor.forClass(OrdenAsignadaEvento.class);
        verify(consumo).alAsignar(evento.capture());
        assertThat(evento.getValue().servicioId()).isEqualTo(3L);
        verify(canal).basicAck(7L, false);
        verify(canal, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void mensajeInvalidoVaALaDlq() throws Exception {
        consumidor.consumir("no es json", canal, 8L);

        verify(consumo, never()).alAsignar(any());
        verify(canal).basicNack(8L, false, false);
    }

    @Test
    void errorAlDescontarVaALaDlq() throws Exception {
        when(consumo.alAsignar(any())).thenThrow(new IllegalStateException("base caida"));

        consumidor.consumir("{\"tipoEvento\":\"ASIGNADA\",\"ordenId\":5,\"empresaId\":1}", canal, 9L);

        verify(canal).basicNack(9L, false, false);
    }
}
