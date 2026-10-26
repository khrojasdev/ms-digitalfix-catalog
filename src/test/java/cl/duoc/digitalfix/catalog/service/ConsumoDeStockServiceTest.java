package cl.duoc.digitalfix.catalog.service;

import cl.duoc.digitalfix.catalog.domain.ConsumoDeOrden;
import cl.duoc.digitalfix.catalog.domain.ConsumoDeOrden.Resultado;
import cl.duoc.digitalfix.catalog.domain.Repuesto;
import cl.duoc.digitalfix.catalog.domain.Servicio;
import cl.duoc.digitalfix.catalog.domain.ServicioRepuesto;
import cl.duoc.digitalfix.catalog.repository.ConsumoDeOrdenRepository;
import cl.duoc.digitalfix.catalog.repository.RepuestoRepository;
import cl.duoc.digitalfix.catalog.repository.ServicioRepository;
import cl.duoc.digitalfix.catalog.repository.ServicioRepuestoRepository;
import cl.duoc.digitalfix.catalog.service.dto.OrdenAsignadaEvento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** Descuento de stock al asignar una orden, contra la base en memoria. */
@DataJpaTest
@ActiveProfiles("test")
@Import(ConsumoDeStockService.class)
class ConsumoDeStockServiceTest {

    private static final Long EMPRESA = 1L;

    @Autowired ConsumoDeStockService servicio;
    @Autowired ServicioRepository servicios;
    @Autowired RepuestoRepository repuestos;
    @Autowired ServicioRepuestoRepository serviciosRepuestos;
    @Autowired ConsumoDeOrdenRepository consumos;
    @MockBean StockAlertaPublisher alertas;

    private Long servicioId;
    private Long breaker;
    private Long cable;

    @BeforeEach
    void catalogo() {
        servicioId = servicios.save(new Servicio(EMPRESA, "INST-01", "Instalacion", null, new BigDecimal("1000"))).getId();
        breaker = repuestos.save(new Repuesto(EMPRESA, "SKU-B", "Breaker", 5, 2, new BigDecimal("100"))).getId();
        cable = repuestos.save(new Repuesto(EMPRESA, "SKU-C", "Cable", 10, 1, new BigDecimal("50"))).getId();
        serviciosRepuestos.save(new ServicioRepuesto(servicioId, breaker, 2));
        serviciosRepuestos.save(new ServicioRepuesto(servicioId, cable, 3));
    }

    private OrdenAsignadaEvento asignada(long orden, String anterior) {
        return new OrdenAsignadaEvento("k-" + orden + "-" + anterior, "ASIGNADA", orden, EMPRESA, servicioId, anterior);
    }

    private int stock(Long repuesto) {
        return repuestos.findByIdAndCompanyId(repuesto, EMPRESA).orElseThrow().getStock();
    }

    @Test
    void descuentaLosRepuestosDelServicio() {
        Optional<ConsumoDeOrden> r = servicio.alAsignar(asignada(10, "CREADA"));

        assertThat(r).isPresent();
        assertThat(r.get().getResultado()).isEqualTo(Resultado.DESCONTADO);
        assertThat(stock(breaker)).isEqualTo(3);
        assertThat(stock(cable)).isEqualTo(7);
        verify(alertas, atLeastOnce()).publicarSiBajoMinimo(any(Repuesto.class));
    }

    @Test
    void elMismoEventoDosVecesDescuentaUnaSola() {
        servicio.alAsignar(asignada(11, "CREADA"));
        Optional<ConsumoDeOrden> segunda = servicio.alAsignar(asignada(11, "CREADA"));

        assertThat(segunda).isEmpty();
        assertThat(stock(breaker)).isEqualTo(3);
        assertThat(consumos.count()).isEqualTo(1);
    }

    @Test
    void unaReasignacionNoVuelveADescontar() {
        servicio.alAsignar(asignada(12, "CREADA"));
        assertThat(servicio.alAsignar(asignada(12, "ASIGNADA"))).isEmpty();
        assertThat(stock(cable)).isEqualTo(7);
    }

    @Test
    void siNoAlcanzaNoDescuentaNinguno() {
        servicio.alAsignar(asignada(13, "CREADA"));   // breaker 5 -> 3
        servicio.alAsignar(asignada(14, "CREADA"));   // breaker 3 -> 1, cable 7 -> 4
        ConsumoDeOrden tercera = servicio.alAsignar(asignada(15, "CREADA")).orElseThrow();

        assertThat(tercera.getResultado()).isEqualTo(Resultado.STOCK_INSUFICIENTE);
        assertThat(tercera.getDetalle()).contains("SKU-B");
        assertThat(stock(breaker)).isEqualTo(1);
        assertThat(stock(cable)).isEqualTo(4);        // no se toco aunque alcanzaba
    }

    @Test
    void servicioSinRepuestosQuedaRegistrado() {
        Long otro = servicios.save(new Servicio(EMPRESA, "VIS-01", "Visita", null, new BigDecimal("500"))).getId();
        ConsumoDeOrden r = servicio.alAsignar(
                new OrdenAsignadaEvento("k", "ASIGNADA", 16L, EMPRESA, otro, "CREADA")).orElseThrow();
        assertThat(r.getResultado()).isEqualTo(Resultado.SIN_REPUESTOS);
    }

    @Test
    void servicioDeOtraEmpresaNoDescuenta() {
        ConsumoDeOrden r = servicio.alAsignar(
                new OrdenAsignadaEvento("k", "ASIGNADA", 17L, 99L, servicioId, "CREADA")).orElseThrow();
        assertThat(r.getResultado()).isEqualTo(Resultado.SERVICIO_NO_ENCONTRADO);
        assertThat(stock(breaker)).isEqualTo(5);
    }

    @Test
    void ignoraOtrosTiposDeEvento() {
        assertThat(servicio.alAsignar(
                new OrdenAsignadaEvento("k", "CERRADA", 18L, EMPRESA, servicioId, "EN_EJECUCION"))).isEmpty();
        assertThat(consumos.count()).isZero();
        verify(alertas, never()).publicarSiBajoMinimo(any(Repuesto.class));
    }
}
