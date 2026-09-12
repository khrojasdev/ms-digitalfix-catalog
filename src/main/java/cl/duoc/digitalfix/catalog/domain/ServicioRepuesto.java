package cl.duoc.digitalfix.catalog.domain;

import jakarta.persistence.*;

/** Cuantos repuestos de cada tipo consume un servicio. Tabla SERVICE_PART. */
@Entity
@Table(name = "SERVICE_PART")
public class ServicioRepuesto {

    @EmbeddedId
    private ServicioRepuestoId id;

    @Column(name = "CANTIDAD", nullable = false)
    private int cantidad;

    protected ServicioRepuesto() { }

    public ServicioRepuesto(Long servicioId, Long repuestoId, int cantidad) {
        this.id = new ServicioRepuestoId(servicioId, repuestoId);
        this.cantidad = cantidad;
    }

    public void cambiarCantidad(int cantidad) { this.cantidad = cantidad; }

    public ServicioRepuestoId getId() { return id; }
    public Long getServicioId() { return id.getServicioId(); }
    public Long getRepuestoId() { return id.getRepuestoId(); }
    public int getCantidad() { return cantidad; }
}
