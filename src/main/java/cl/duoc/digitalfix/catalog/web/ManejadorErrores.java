package cl.duoc.digitalfix.catalog.web;

import cl.duoc.digitalfix.catalog.error.*;
import cl.duoc.digitalfix.catalog.web.dto.RespuestaError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/** Todo error sale con el mismo cuerpo y sin filtrar detalles internos. */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(RecursoNoEncontrado.class)
    public ResponseEntity<RespuestaError> noEncontrado(RecursoNoEncontrado e, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(RespuestaError.de("NOT_FOUND", e.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(SolicitudInvalida.class)
    public ResponseEntity<RespuestaError> invalida(SolicitudInvalida e, HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(RespuestaError.de("BAD_REQUEST", e.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> inesperado(Exception e, HttpServletRequest req) {
        log.error("error no controlado en {}", req.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RespuestaError.de(
                "INTERNAL_ERROR", "ocurrio un error inesperado", req.getRequestURI()));
    }
}
