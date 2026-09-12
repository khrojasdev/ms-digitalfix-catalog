package cl.duoc.digitalfix.catalog.repository;

import cl.duoc.digitalfix.catalog.domain.Servicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Con datos de dos empresas en la misma tabla, ninguna consulta de la empresa A
 * puede devolver una fila de la empresa B.
 */
@DataJpaTest
@ActiveProfiles("test")
class AislamientoPorEmpresaTest {

    private static final Long EMPRESA_A = 1L;
    private static final Long EMPRESA_B = 2L;

    @Autowired ServicioRepository servicios;

    @BeforeEach
    void datosDeDosEmpresas() {
        servicios.save(new Servicio(EMPRESA_A, "MANT-01", "Mantencion A", null, new BigDecimal("1000")));
        servicios.save(new Servicio(EMPRESA_A, "MANT-02", "Revision A",   null, new BigDecimal("2000")));
        servicios.save(new Servicio(EMPRESA_B, "MANT-01", "Mantencion B", null, new BigDecimal("3000")));
    }

    @Test
    @DisplayName("el listado de A no incluye nada de B")
    void listadoAislado() {
        var deA = servicios.findByCompanyId(EMPRESA_A, PageRequest.of(0, 20));
        assertThat(deA.getTotalElements()).isEqualTo(2);
        assertThat(deA.getContent()).allMatch(s -> s.getCompanyId().equals(EMPRESA_A));
    }

    @Test
    @DisplayName("buscar por id un servicio de B desde A no devuelve nada")
    void idDeOtraEmpresaNoAparece() {
        Servicio deB = servicios.findByCompanyId(EMPRESA_B, PageRequest.of(0, 20)).getContent().get(0);
        assertThat(servicios.findByIdAndCompanyId(deB.getId(), EMPRESA_A)).isEmpty();
        assertThat(servicios.findByIdAndCompanyId(deB.getId(), EMPRESA_B)).isPresent();
    }
}
