package cl.duoc.digitalfix.catalog.repository;

import cl.duoc.digitalfix.catalog.domain.Servicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * NINGUN metodo permite consultar sin filtro de empresa. No hay findById a secas
 * ni findAll: esa ausencia es deliberada, para que sea imposible escribir por
 * descuido una consulta que cruce empresas.
 */
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    Optional<Servicio> findByIdAndCompanyId(Long id, Long companyId);

    Page<Servicio> findByCompanyId(Long companyId, Pageable pageable);

}
