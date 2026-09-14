package cl.duoc.digitalfix.catalog.web.dto;

import cl.duoc.digitalfix.catalog.domain.Servicio;
import java.math.BigDecimal;

public record ServicioRespuesta(Long id, String codigo, String nombre, String descripcion,
                                BigDecimal tarifa, boolean activo) {

    public static ServicioRespuesta de(Servicio s) {
        return new ServicioRespuesta(s.getId(), s.getCodigo(), s.getNombre(),
                                     s.getDescripcion(), s.getTarifa(), s.isActivo());
    }
}
