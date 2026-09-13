package cl.duoc.digitalfix.catalog.repository;

import cl.duoc.digitalfix.catalog.domain.Repuesto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepuestoRepository extends JpaRepository<Repuesto, Long> {

    Optional<Repuesto> findByIdAndCompanyId(Long id, Long companyId);

    Page<Repuesto> findByCompanyId(Long companyId, Pageable pageable);

    boolean existsByCompanyIdAndSku(Long companyId, String sku);

    List<Repuesto> findByCompanyIdAndIdIn(Long companyId, List<Long> ids);

    /** Repuestos en o bajo su minimo. La comparacion va en la consulta, no en memoria. */
    @Query("select r from Repuesto r where r.companyId = :companyId and r.activo = true "
         + "and r.stock <= r.stockMinimo order by r.stock asc, r.sku asc")
    List<Repuesto> findBajoMinimo(@Param("companyId") Long companyId);
}
