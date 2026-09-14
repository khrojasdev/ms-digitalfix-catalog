package cl.duoc.digitalfix.catalog.repository;

import cl.duoc.digitalfix.catalog.domain.Repuesto;
import cl.duoc.digitalfix.catalog.domain.Servicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * La prueba que importa de verdad: con datos de dos empresas en la misma tabla,
 * ninguna consulta de la empresa A puede devolver una fila de la empresa B.
 */
@DataJpaTest
@ActiveProfiles("test")
class AislamientoPorEmpresaTest {

    private static final Long EMPRESA_A = 1L;
    private static final Long EMPRESA_B = 2L;

    @Autowired ServicioRepository servicios;
    @Autowired RepuestoRepository repuestos;

    @BeforeEach
    void datosDeDosEmpresas() {
        servicios.save(new Servicio(EMPRESA_A, "MANT-01", "Mantencion A", null, new BigDecimal("1000")));
        servicios.save(new Servicio(EMPRESA_A, "MANT-02", "Revision A",   null, new BigDecimal("2000")));
        servicios.save(new Servicio(EMPRESA_B, "MANT-01", "Mantencion B", null, new BigDecimal("3000")));

        repuestos.save(new Repuesto(EMPRESA_A, "SKU-1", "Breaker A",  1, 5, new BigDecimal("100")));
        repuestos.save(new Repuesto(EMPRESA_A, "SKU-2", "Cable A",   50, 5, new BigDecimal("200")));
        repuestos.save(new Repuesto(EMPRESA_B, "SKU-1", "Breaker B",  0, 9, new BigDecimal("300")));
    }

    @Test
    @DisplayName("el listado de A no incluye nada de B")
    void listadoAislado() {
        var deA = servicios.findByCompanyId(EMPRESA_A, PageRequest.of(0, 20));
        assertThat(deA.getTotalElements()).isEqualTo(2);
        assertThat(deA.getContent()).allMatch(s -> s.getCompanyId().equals(EMPRESA_A));
    }

    @Test
    @DisplayName("el mismo codigo puede repetirse entre empresas distintas")
    void codigoUnicoPorEmpresaNoGlobal() {
        assertThat(servicios.existsByCompanyIdAndCodigo(EMPRESA_A, "MANT-01")).isTrue();
        assertThat(servicios.existsByCompanyIdAndCodigo(EMPRESA_B, "MANT-01")).isTrue();
        assertThat(servicios.existsByCompanyIdAndCodigo(EMPRESA_A, "MANT-99")).isFalse();
    }

    @Test
    @DisplayName("buscar por id un servicio de B desde A no devuelve nada")
    void idDeOtraEmpresaNoAparece() {
        Servicio deB = servicios.findByCompanyId(EMPRESA_B, PageRequest.of(0, 20)).getContent().get(0);
        assertThat(servicios.findByIdAndCompanyId(deB.getId(), EMPRESA_A)).isEmpty();
        assertThat(servicios.findByIdAndCompanyId(deB.getId(), EMPRESA_B)).isPresent();
    }

    @Test
    @DisplayName("los repuestos bajo minimo son solo los de la empresa que consulta")
    void bajoMinimoAislado() {
        List<Repuesto> deA = repuestos.findBajoMinimo(EMPRESA_A);
        assertThat(deA).hasSize(1);
        assertThat(deA.get(0).getSku()).isEqualTo("SKU-1");
        assertThat(deA.get(0).getCompanyId()).isEqualTo(EMPRESA_A);

        assertThat(repuestos.findBajoMinimo(EMPRESA_B)).hasSize(1);
    }

    @Test
    @DisplayName("findByCompanyIdAndIdIn descarta los ids que son de otra empresa")
    void idsAjenosSeDescartan() {
        Repuesto deB = repuestos.findBajoMinimo(EMPRESA_B).get(0);
        Repuesto deA = repuestos.findBajoMinimo(EMPRESA_A).get(0);

        List<Repuesto> encontrados =
                repuestos.findByCompanyIdAndIdIn(EMPRESA_A, List.of(deA.getId(), deB.getId()));

        assertThat(encontrados).hasSize(1);
        assertThat(encontrados.get(0).getId()).isEqualTo(deA.getId());
    }
}
