package cl.duoc.digitalfix.catalog.web;

import cl.duoc.digitalfix.catalog.domain.Servicio;
import cl.duoc.digitalfix.catalog.error.ConflictoDeDatos;
import cl.duoc.digitalfix.catalog.error.RecursoNoEncontrado;
import cl.duoc.digitalfix.catalog.service.ServicioRepuestoService;
import cl.duoc.digitalfix.catalog.service.ServicioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServicioController.class)
class ServicioControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @MockBean ServicioService servicios;
    @MockBean ServicioRepuestoService relaciones;

    private Servicio unServicio() {
        return new Servicio(1L, "MANT-01", "Mantencion preventiva", "Revision anual",
                            new BigDecimal("85000.00"));
    }

    @Test
    @DisplayName("GET devuelve la lista de la empresa del contexto")
    void listaServicios() throws Exception {
        Page<Servicio> pagina = new PageImpl<>(List.of(unServicio()));
        when(servicios.listar(any(), any())).thenReturn(pagina);

        mvc.perform(get("/api/catalog/services").header("X-Company-Id", "1"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.content[0].codigo").value("MANT-01"))
           .andExpect(jsonPath("$.content[0].nombre").value("Mantencion preventiva"));
    }

    @Test
    @DisplayName("un id inexistente o de otra empresa responde 404 con cuerpo uniforme")
    void noEncontradoEs404() throws Exception {
        when(servicios.obtener(99L)).thenThrow(new RecursoNoEncontrado("no existe el servicio 99"));

        mvc.perform(get("/api/catalog/services/99").header("X-Company-Id", "1"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.error").value("NOT_FOUND"))
           .andExpect(jsonPath("$.path").value("/api/catalog/services/99"))
           .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("un codigo repetido responde 409")
    void codigoRepetidoEs409() throws Exception {
        when(servicios.crear(any())).thenThrow(new ConflictoDeDatos("ya existe un servicio con el codigo MANT-01"));

        mvc.perform(post("/api/catalog/services")
                .header("X-Company-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "codigo", "MANT-01", "nombre", "Mantencion", "tarifa", "85000.00"))))
           .andExpect(status().isConflict())
           .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    @DisplayName("una tarifa de cero o negativa se rechaza con 400 y detalle del campo")
    void tarifaInvalidaEs400() throws Exception {
        mvc.perform(post("/api/catalog/services")
                .header("X-Company-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "codigo", "MANT-01", "nombre", "Mantencion", "tarifa", "0"))))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
           .andExpect(jsonPath("$.detalles[0]").value(org.hamcrest.Matchers.containsString("tarifa")));
    }

    @Test
    @DisplayName("faltar el codigo se rechaza con 400")
    void codigoObligatorio() throws Exception {
        mvc.perform(post("/api/catalog/services")
                .header("X-Company-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("nombre", "Mantencion", "tarifa", "1000"))))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("desactivar responde 204 sin cuerpo")
    void desactivarEs204() throws Exception {
        mvc.perform(delete("/api/catalog/services/1").header("X-Company-Id", "1"))
           .andExpect(status().isNoContent());
    }
}
