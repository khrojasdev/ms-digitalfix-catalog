package cl.duoc.digitalfix.catalog.config;

import java.util.List;

/**
 * Identidad de quien llama, resuelta por el BFF y propagada en cabeceras.
 *
 * Este microservicio NUNCA acepta el companyId desde el cuerpo ni desde el query
 * string: la empresa sale del contexto y de ningun otro lugar. Esa es la unica
 * garantia de que un usuario no pueda leer ni tocar datos de otra empresa.
 */
public record ContextoEmpresa(Long companyId, String usuarioOid, List<String> roles) {

    public boolean tieneRol(String rol) {
        return roles != null && roles.contains(rol);
    }
}
