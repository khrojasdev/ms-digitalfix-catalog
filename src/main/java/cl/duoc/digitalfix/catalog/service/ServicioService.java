package cl.duoc.digitalfix.catalog.service;

import cl.duoc.digitalfix.catalog.domain.Servicio;
import cl.duoc.digitalfix.catalog.error.ConflictoDeDatos;
import cl.duoc.digitalfix.catalog.error.RecursoNoEncontrado;
import cl.duoc.digitalfix.catalog.repository.ServicioRepository;
import cl.duoc.digitalfix.catalog.web.dto.ServicioSolicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioService {

    private final ServicioRepository repositorio;
    private final ContextoService contexto;

    public ServicioService(ServicioRepository repositorio, ContextoService contexto) {
        this.repositorio = repositorio;
        this.contexto = contexto;
    }

    @Transactional(readOnly = true)
    public Page<Servicio> listar(Pageable pagina) {
        return repositorio.findByCompanyId(contexto.companyId(), pagina);
    }

    /** Un id de otra empresa se comporta igual que uno inexistente: 404. */
    @Transactional(readOnly = true)
    public Servicio obtener(Long id) {
        return repositorio.findByIdAndCompanyId(id, contexto.companyId())
                .orElseThrow(() -> new RecursoNoEncontrado("no existe el servicio " + id));
    }

    @Transactional
    public Servicio crear(ServicioSolicitud solicitud) {
        Long empresa = contexto.companyId();
        if (repositorio.existsByCompanyIdAndCodigo(empresa, solicitud.codigo())) {
            throw new ConflictoDeDatos("ya existe un servicio con el codigo " + solicitud.codigo());
        }
        return repositorio.save(new Servicio(empresa, solicitud.codigo(), solicitud.nombre(),
                                             solicitud.descripcion(), solicitud.tarifa()));
    }

    @Transactional
    public Servicio actualizar(Long id, ServicioSolicitud solicitud) {
        Servicio servicio = obtener(id);
        if (!servicio.getCodigo().equals(solicitud.codigo())
                && repositorio.existsByCompanyIdAndCodigo(servicio.getCompanyId(), solicitud.codigo())) {
            throw new ConflictoDeDatos("ya existe un servicio con el codigo " + solicitud.codigo());
        }
        servicio.actualizar(solicitud.nombre(), solicitud.descripcion(), solicitud.tarifa());
        return repositorio.save(servicio);
    }

}
