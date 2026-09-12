package cl.duoc.digitalfix.catalog.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/** Servicio tecnico del catalogo de una empresa. Tabla SERVICE. */
@Entity
@Table(name = "SERVICE",
       uniqueConstraints = @UniqueConstraint(name = "UK_SERVICE_CODIGO",
                                             columnNames = {"COMPANY_ID", "CODIGO"}))
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Referencia a DFX_USUARIOS.COMPANY. Sin clave foranea: es otro esquema. */
    @Column(name = "COMPANY_ID", nullable = false)
    private Long companyId;

    @Column(name = "CODIGO", nullable = false, length = 30)
    private String codigo;

    @Column(name = "NOMBRE", nullable = false, length = 120)
    private String nombre;

    @Column(name = "DESCRIPCION", length = 500)
    private String descripcion;

    @Column(name = "TARIFA", nullable = false, precision = 12, scale = 2)
    private BigDecimal tarifa;

    @Column(name = "ACTIVO", nullable = false)
    private boolean activo = true;

    @Column(name = "CREADO_EN", nullable = false)
    private Instant creadoEn;

    @Column(name = "ACTUALIZADO_EN")
    private Instant actualizadoEn;

    protected Servicio() { }

    public Servicio(Long companyId, String codigo, String nombre, String descripcion, BigDecimal tarifa) {
        this.companyId = companyId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tarifa = tarifa;
        this.activo = true;
    }

    @PrePersist
    void alCrear() { this.creadoEn = Instant.now(); }

    @PreUpdate
    void alActualizar() { this.actualizadoEn = Instant.now(); }

    public void actualizar(String nombre, String descripcion, BigDecimal tarifa) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tarifa = tarifa;
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getTarifa() { return tarifa; }
    public boolean isActivo() { return activo; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
}
