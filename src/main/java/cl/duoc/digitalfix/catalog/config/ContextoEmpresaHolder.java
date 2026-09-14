package cl.duoc.digitalfix.catalog.config;

/** Guarda el contexto de la peticion en curso. Se limpia siempre al terminar. */
public final class ContextoEmpresaHolder {

    private static final ThreadLocal<ContextoEmpresa> ACTUAL = new ThreadLocal<>();

    private ContextoEmpresaHolder() { }

    public static void definir(ContextoEmpresa contexto) { ACTUAL.set(contexto); }

    public static ContextoEmpresa actual() { return ACTUAL.get(); }

    public static void limpiar() { ACTUAL.remove(); }
}
