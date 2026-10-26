package cl.duoc.digitalfix.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Registro de que la orden ya paso por el descuento de stock, y con que
 * resultado. Tabla ORDER_STOCK_USAGE, una fila por orden.
 */
@Entity
@Table(name = "ORDER_STOCK_USAGE",
       uniqueConstraints = @UniqueConstraint(name = "UK_OSU_ORDEN",
                                             columnNames = {"COMPANY_ID", "ORDER_ID"}))
public class ConsumoDeOrden {

    public enum Resultado { DESCONTADO, STOCK_INSUFICIENTE, SIN_REPUESTOS, SERVICIO_NO_ENCONTRADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "COMPANY_ID", nullable = false)
    private Long companyId;

    @Column(name = "ORDER_ID", nullable = false)
    private Long ordenId;

    @Column(name = "SERVICE_ID")
    private Long servicioId;

    @Column(name = "EVENT_KEY", nullable = false, length = 64)
    private String eventKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "RESULTADO", nullable = false, length = 30)
    private Resultado resultado;

    @Column(name = "DETALLE", length = 500)
    private String detalle;

    @Column(name = "CREADO_EN", nullable = false)
    private Instant creadoEn;

    protected ConsumoDeOrden() { }

    public ConsumoDeOrden(Long companyId, Long ordenId, Long servicioId, String eventKey,
                          Resultado resultado, String detalle) {
        this.companyId = companyId;
        this.ordenId = ordenId;
        this.servicioId = servicioId;
        this.eventKey = eventKey;
        this.resultado = resultado;
        this.detalle = detalle == null || detalle.length() <= 500 ? detalle : detalle.substring(0, 500);
    }

    @PrePersist
    void alCrear() { this.creadoEn = Instant.now(); }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public Long getOrdenId() { return ordenId; }
    public Long getServicioId() { return servicioId; }
    public String getEventKey() { return eventKey; }
    public Resultado getResultado() { return resultado; }
    public String getDetalle() { return detalle; }
    public Instant getCreadoEn() { return creadoEn; }
}
