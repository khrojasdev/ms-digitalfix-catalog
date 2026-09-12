package cl.duoc.digitalfix.catalog.web.dto;

import cl.duoc.digitalfix.catalog.domain.Repuesto;
import java.math.BigDecimal;

public record RepuestoRespuesta(Long id, String sku, String nombre, int stock, int stockMinimo,
                                BigDecimal costoUnitario, boolean activo, boolean bajoMinimo) {

    public static RepuestoRespuesta de(Repuesto r) {
        return new RepuestoRespuesta(r.getId(), r.getSku(), r.getNombre(), r.getStock(),
                                     r.getStockMinimo(), r.getCostoUnitario(), r.isActivo(),
                                     r.estaBajoMinimo());
    }
}
