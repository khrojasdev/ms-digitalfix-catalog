package cl.duoc.digitalfix.catalog.service;

import cl.duoc.digitalfix.catalog.domain.Repuesto;
import cl.duoc.digitalfix.catalog.domain.ServicioRepuesto;
import cl.duoc.digitalfix.catalog.domain.ServicioRepuestoId;
import cl.duoc.digitalfix.catalog.error.RecursoNoEncontrado;
import cl.duoc.digitalfix.catalog.repository.RepuestoRepository;
import cl.duoc.digitalfix.catalog.repository.ServicioRepuestoRepository;
import cl.duoc.digitalfix.catalog.web.dto.RepuestoDeServicio;
import cl.duoc.digitalfix.catalog.web.dto.RepuestoDeServicioRespuesta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ServicioRepuestoService {

    private final ServicioRepuestoRepository relaciones;
    private final RepuestoRepository repuestos;
    private final ServicioService servicios;
    private final ContextoService contexto;

    public ServicioRepuestoService(ServicioRepuestoRepository relaciones, RepuestoRepository repuestos,
                                   ServicioService servicios, ContextoService contexto) {
        this.relaciones = relaciones;
        this.repuestos = repuestos;
        this.servicios = servicios;
        this.contexto = contexto;
    }

    /**
     * Asociacion idempotente: si el par servicio-repuesto ya existe, se actualiza
     * la cantidad en vez de insertar otra fila. La clave compuesta lo garantiza.
     */
    @Transactional
    public List<RepuestoDeServicioRespuesta> asociar(Long servicioId, List<RepuestoDeServicio> pedidos) {
        servicios.obtener(servicioId);                 // valida que el servicio sea de la empresa
        Long empresa = contexto.companyId();

        List<Long> ids = pedidos.stream().map(RepuestoDeServicio::repuestoId).distinct().toList();
        Map<Long, Repuesto> encontrados = repuestos.findByCompanyIdAndIdIn(empresa, ids).stream()
                .collect(Collectors.toMap(Repuesto::getId, Function.identity()));

        for (Long id : ids) {
            if (!encontrados.containsKey(id)) {
                throw new RecursoNoEncontrado("no existe el repuesto " + id);
            }
        }

        for (RepuestoDeServicio pedido : pedidos) {
            ServicioRepuestoId clave = new ServicioRepuestoId(servicioId, pedido.repuestoId());
            ServicioRepuesto relacion = relaciones.findById(clave)
                    .orElseGet(() -> new ServicioRepuesto(servicioId, pedido.repuestoId(), pedido.cantidad()));
            relacion.cambiarCantidad(pedido.cantidad());
            relaciones.save(relacion);
        }
        return listar(servicioId);
    }

    @Transactional(readOnly = true)
    public List<RepuestoDeServicioRespuesta> listar(Long servicioId) {
        servicios.obtener(servicioId);
        List<ServicioRepuesto> filas = relaciones.findByIdServicioId(servicioId);
        if (filas.isEmpty()) return List.of();

        Map<Long, Repuesto> porId = repuestos
                .findByCompanyIdAndIdIn(contexto.companyId(),
                                        filas.stream().map(ServicioRepuesto::getRepuestoId).toList())
                .stream().collect(Collectors.toMap(Repuesto::getId, Function.identity()));

        List<RepuestoDeServicioRespuesta> salida = new ArrayList<>();
        for (ServicioRepuesto fila : filas) {
            Repuesto r = porId.get(fila.getRepuestoId());
            if (r == null) continue;
            salida.add(new RepuestoDeServicioRespuesta(r.getId(), r.getSku(), r.getNombre(),
                                                       fila.getCantidad(), r.getStock()));
        }
        return salida;
    }
}
