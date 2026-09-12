package cl.duoc.digitalfix.catalog.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ServicioSolicitud(
        @NotBlank(message = "el codigo es obligatorio")
        @Size(max = 30, message = "el codigo no puede superar 30 caracteres")
        String codigo,

        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 120, message = "el nombre no puede superar 120 caracteres")
        String nombre,

        @Size(max = 500, message = "la descripcion no puede superar 500 caracteres")
        String descripcion,

        @NotNull(message = "la tarifa es obligatoria")
        @DecimalMin(value = "0.01", message = "la tarifa debe ser mayor que cero")
        @Digits(integer = 10, fraction = 2, message = "la tarifa admite hasta dos decimales")
        BigDecimal tarifa) { }
