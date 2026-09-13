package cl.duoc.digitalfix.catalog.service;

import cl.duoc.digitalfix.catalog.domain.Repuesto;
import cl.duoc.digitalfix.catalog.error.RecursoNoEncontrado;
import cl.duoc.digitalfix.catalog.repository.RepuestoRepository;
import cl.duoc.digitalfix.catalog.web.dto.RepuestoSolicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class RepuestoService {

    private final RepuestoRepository repositorio;
    private final ContextoService contexto;

    public RepuestoService(RepuestoRepository repositorio, ContextoService contexto) {
        this.repositorio = repositorio;
        this.contexto = contexto;
    }

    @Transactional(readOnly = true)
    public Page<Repuesto> listar(Pageable pagina) {
        return repositorio.findByCompanyId(contexto.companyId(), pagina);
    }

    @Transactional(readOnly = true)
    public Repuesto obtener(Long id) {
        return repositorio.findByIdAndCompanyId(id, contexto.companyId())
                .orElseThrow(() -> new RecursoNoEncontrado("no existe el repuesto " + id));
    }

    @Transactional
    public Repuesto crear(RepuestoSolicitud solicitud) {
        Long empresa = contexto.companyId();
        return repositorio.save(new Repuesto(empresa, solicitud.sku(), solicitud.nombre(),
                                             solicitud.stock(), solicitud.stockMinimo(),
                                             solicitud.costoUnitario()));
    }

    @Transactional
    public Repuesto actualizar(Long id, RepuestoSolicitud solicitud) {
        Repuesto repuesto = obtener(id);
        repuesto.actualizar(solicitud.nombre(), solicitud.stock(),
                            solicitud.stockMinimo(), solicitud.costoUnitario());
        return repositorio.save(repuesto);
    }

}
