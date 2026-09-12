package cl.duoc.digitalfix.catalog.error;

/** Choque con una restriccion de unicidad: codigo o SKU repetido. Responde 409. */
public class ConflictoDeDatos extends RuntimeException {
    public ConflictoDeDatos(String mensaje) { super(mensaje); }
}
