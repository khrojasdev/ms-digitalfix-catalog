package cl.duoc.digitalfix.catalog.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Un repuesto y su cantidad dentro de un servicio. */
public record RepuestoDeServicio(
        @NotNull(message = "el identificador del repuesto es obligatorio") Long repuestoId,
        @NotNull(message = "la cantidad es obligatoria")
        @Min(value = 1, message = "la cantidad debe ser mayor que cero") Integer cantidad) { }
