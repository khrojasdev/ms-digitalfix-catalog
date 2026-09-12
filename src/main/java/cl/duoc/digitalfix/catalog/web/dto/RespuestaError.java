package cl.duoc.digitalfix.catalog.web.dto;

import java.time.Instant;
import java.util.List;

/** Cuerpo uniforme de error. Nunca expone stacktrace ni detalles internos. */
public record RespuestaError(String error, String message, Instant timestamp,
                             String path, List<String> detalles) {

    public static RespuestaError de(String error, String mensaje, String path) {
        return new RespuestaError(error, mensaje, Instant.now(), path, null);
    }

    public static RespuestaError de(String error, String mensaje, String path, List<String> detalles) {
        return new RespuestaError(error, mensaje, Instant.now(), path, detalles);
    }
}
