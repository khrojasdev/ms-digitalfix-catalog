package cl.duoc.digitalfix.catalog.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record RepuestoSolicitud(
        @NotBlank(message = "el SKU es obligatorio")
        @Size(max = 40, message = "el SKU no puede superar 40 caracteres")
        String sku,

        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 120, message = "el nombre no puede superar 120 caracteres")
        String nombre,

        @NotNull(message = "el stock es obligatorio")
        @Min(value = 0, message = "el stock no puede ser negativo")
        Integer stock,

        @NotNull(message = "el stock minimo es obligatorio")
        @Min(value = 0, message = "el stock minimo no puede ser negativo")
        Integer stockMinimo,

        @DecimalMin(value = "0.00", message = "el costo unitario no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "el costo admite hasta dos decimales")
        BigDecimal costoUnitario) { }
