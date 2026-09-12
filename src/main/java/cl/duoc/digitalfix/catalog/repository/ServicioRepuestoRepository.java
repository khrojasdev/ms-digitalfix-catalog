package cl.duoc.digitalfix.catalog.repository;

import cl.duoc.digitalfix.catalog.domain.ServicioRepuesto;
import cl.duoc.digitalfix.catalog.domain.ServicioRepuestoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioRepuestoRepository extends JpaRepository<ServicioRepuesto, ServicioRepuestoId> {

    List<ServicioRepuesto> findByIdServicioId(Long servicioId);

}
