package cl.duoc.digitalfix.catalog.repository;

import cl.duoc.digitalfix.catalog.domain.ConsumoDeOrden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsumoDeOrdenRepository extends JpaRepository<ConsumoDeOrden, Long> {

    boolean existsByCompanyIdAndOrdenId(Long companyId, Long ordenId);

    Optional<ConsumoDeOrden> findByCompanyIdAndOrdenId(Long companyId, Long ordenId);
}
