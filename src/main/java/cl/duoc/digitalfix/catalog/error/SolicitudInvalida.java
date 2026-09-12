package cl.duoc.digitalfix.catalog.error;

/** Regla de negocio incumplida que no cubre Bean Validation. Responde 400. */
public class SolicitudInvalida extends RuntimeException {
    public SolicitudInvalida(String mensaje) { super(mensaje); }
}
