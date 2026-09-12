package cl.duoc.digitalfix.catalog.service;

import cl.duoc.digitalfix.catalog.config.ContextoEmpresa;
import cl.duoc.digitalfix.catalog.config.ContextoEmpresaHolder;
import cl.duoc.digitalfix.catalog.error.SolicitudInvalida;
import org.springframework.stereotype.Component;

/** Unico punto desde donde se obtiene la empresa. Si no hay contexto, nada avanza. */
@Component
public class ContextoService {

    public ContextoEmpresa actual() {
        ContextoEmpresa ctx = ContextoEmpresaHolder.actual();
        if (ctx == null || ctx.companyId() == null) {
            throw new SolicitudInvalida(
                "falta la cabecera X-Company-Id; este servicio se consume a traves del BFF");
        }
        return ctx;
    }

    public Long companyId() { return actual().companyId(); }
}
