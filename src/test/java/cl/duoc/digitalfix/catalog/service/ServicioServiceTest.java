package cl.duoc.digitalfix.catalog.service;

import cl.duoc.digitalfix.catalog.domain.Servicio;
import cl.duoc.digitalfix.catalog.error.ConflictoDeDatos;
import cl.duoc.digitalfix.catalog.error.RecursoNoEncontrado;
import cl.duoc.digitalfix.catalog.repository.ServicioRepository;
import cl.duoc.digitalfix.catalog.web.dto.ServicioSolicitud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    private static final Long EMPRESA = 7L;

    @Mock  ServicioRepository repositorio;
    @Mock  ContextoService contexto;
    @InjectMocks ServicioService servicio;

    private ServicioSolicitud solicitud() {
        return new ServicioSolicitud("MANT-01", "Mantencion preventiva",
                                     "Revision anual", new BigDecimal("85000.00"));
    }

    @BeforeEach
    void contextoDeEmpresa() {
        lenient().when(contexto.companyId()).thenReturn(EMPRESA);
    }

    @Test
    @DisplayName("crear guarda el servicio con la empresa del contexto, no la que venga del cliente")
    void creaConEmpresaDelContexto() {
        when(repositorio.existsByCompanyIdAndCodigo(EMPRESA, "MANT-01")).thenReturn(false);
        when(repositorio.save(any(Servicio.class))).thenAnswer(i -> i.getArgument(0));

        Servicio creado = servicio.crear(solicitud());

        assertThat(creado.getCompanyId()).isEqualTo(EMPRESA);
        assertThat(creado.isActivo()).isTrue();
    }

    @Test
    @DisplayName("un codigo repetido en la misma empresa devuelve conflicto")
    void rechazaCodigoDuplicado() {
        when(repositorio.existsByCompanyIdAndCodigo(EMPRESA, "MANT-01")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear(solicitud()))
                .isInstanceOf(ConflictoDeDatos.class)
                .hasMessageContaining("MANT-01");
        verify(repositorio, never()).save(any());
    }

    @Test
    @DisplayName("pedir un servicio de otra empresa se comporta como si no existiera")
    void servicioDeOtraEmpresaEs404() {
        when(repositorio.findByIdAndCompanyId(99L, EMPRESA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtener(99L))
                .isInstanceOf(RecursoNoEncontrado.class);
    }

    @Test
    @DisplayName("desactivar no borra: deja el servicio inactivo")
    void desactivaSinBorrar() {
        Servicio existente = new Servicio(EMPRESA, "MANT-01", "Mantencion", null, new BigDecimal("1000"));
        when(repositorio.findByIdAndCompanyId(1L, EMPRESA)).thenReturn(Optional.of(existente));
        when(repositorio.save(any(Servicio.class))).thenAnswer(i -> i.getArgument(0));

        servicio.desactivar(1L);

        assertThat(existente.isActivo()).isFalse();
        verify(repositorio, never()).delete(any());
        verify(repositorio, never()).deleteById(any());
    }

    @Test
    @DisplayName("cambiar el codigo por uno ya usado devuelve conflicto")
    void rechazaCambioACodigoExistente() {
        Servicio existente = new Servicio(EMPRESA, "MANT-01", "Mantencion", null, new BigDecimal("1000"));
        when(repositorio.findByIdAndCompanyId(1L, EMPRESA)).thenReturn(Optional.of(existente));
        when(repositorio.existsByCompanyIdAndCodigo(EMPRESA, "MANT-02")).thenReturn(true);

        ServicioSolicitud cambio = new ServicioSolicitud("MANT-02", "Otro", null, new BigDecimal("2000"));
        assertThatThrownBy(() -> servicio.actualizar(1L, cambio))
                .isInstanceOf(ConflictoDeDatos.class);
    }
}
