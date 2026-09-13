package cl.duoc.digitalfix.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** Clave compuesta de SERVICE_PART. Es lo que hace idempotente la asociacion. */
@Embeddable
public class ServicioRepuestoId implements Serializable {

    @Column(name = "SERVICE_ID")
    private Long servicioId;

    @Column(name = "PART_ID")
    private Long repuestoId;

    protected ServicioRepuestoId() { }

    public ServicioRepuestoId(Long servicioId, Long repuestoId) {
        this.servicioId = servicioId;
        this.repuestoId = repuestoId;
    }

    public Long getServicioId() { return servicioId; }
    public Long getRepuestoId() { return repuestoId; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServicioRepuestoId otro)) return false;
        return Objects.equals(servicioId, otro.servicioId) && Objects.equals(repuestoId, otro.repuestoId);
    }

    @Override public int hashCode() { return Objects.hash(servicioId, repuestoId); }
}
