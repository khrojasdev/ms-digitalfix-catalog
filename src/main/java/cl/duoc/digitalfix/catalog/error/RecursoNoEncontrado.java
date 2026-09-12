package cl.duoc.digitalfix.catalog.error;

/**
 * Se lanza tanto cuando el recurso no existe como cuando pertenece a otra
 * empresa. Ambos casos responden 404 a proposito: un 403 revelaria que el
 * recurso existe.
 */
public class RecursoNoEncontrado extends RuntimeException {
    public RecursoNoEncontrado(String mensaje) { super(mensaje); }
}
