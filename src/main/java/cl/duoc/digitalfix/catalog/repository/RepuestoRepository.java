package cl.duoc.digitalfix.catalog.repository;

import cl.duoc.digitalfix.catalog.domain.Repuesto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepuestoRepository extends JpaRepository<Repuesto, Long> {

    Optional<Repuesto> findByIdAndCompanyId(Long id, Long companyId);

    Page<Repuesto> findByCompanyId(Long companyId, Pageable pageable);

    boolean existsByCompanyIdAndSku(Long companyId, String sku);

    List<Repuesto> findByCompanyIdAndIdIn(Long companyId, List<Long> ids);

}
