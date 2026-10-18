package cl.duoc.digitalfix.catalog.service.dto;

import java.time.Instant;

/**
 * Alerta que se publica cuando un repuesto queda en o bajo su stock minimo.
 * Viaja como mensaje por el exchange direct de stock.
 */
public record AlertaStockDto(
        Long repuestoId,
        Long empresaId,
        String sku,
        String nombre,
        int stock,
        int stockMinimo,
        Instant fecha
) {}
