package cl.duoc.digitalfix.catalog.error;

/** No alcanza el stock para comprometer los repuestos pedidos. Responde 409. */
public class StockInsuficiente extends RuntimeException {
    public StockInsuficiente(String mensaje) { super(mensaje); }
}
