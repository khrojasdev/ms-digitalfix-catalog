package cl.duoc.digitalfix.catalog.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Lee X-Company-Id, X-User-Oid y X-Roles, que pone el BFF despues de validar el
 * token. Si falta la empresa, la peticion no avanza: es preferible un 400 claro
 * a una consulta sin filtro de empresa.
 */
@Component
@Order(1)
public class ContextoEmpresaFilter extends OncePerRequestFilter {

    public static final String CABECERA_EMPRESA = "X-Company-Id";
    public static final String CABECERA_USUARIO = "X-User-Oid";
    public static final String CABECERA_ROLES   = "X-Roles";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        try {
            String empresa = req.getHeader(CABECERA_EMPRESA);
            if (empresa != null && !empresa.isBlank()) {
                List<String> roles = List.of();
                String cabeceraRoles = req.getHeader(CABECERA_ROLES);
                if (cabeceraRoles != null && !cabeceraRoles.isBlank()) {
                    roles = Arrays.stream(cabeceraRoles.split(","))
                                  .map(String::trim)
                                  .filter(s -> !s.isEmpty())
                                  .toList();
                }
                try {
                    ContextoEmpresaHolder.definir(new ContextoEmpresa(
                            Long.valueOf(empresa.trim()), req.getHeader(CABECERA_USUARIO), roles));
                } catch (NumberFormatException ignorada) {
                    // contexto ausente: el servicio respondera 400
                }
            }
            chain.doFilter(req, res);
        } finally {
            ContextoEmpresaHolder.limpiar();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        return req.getRequestURI().startsWith("/actuator");
    }
}
