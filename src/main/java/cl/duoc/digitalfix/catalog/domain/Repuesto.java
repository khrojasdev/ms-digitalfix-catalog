package cl.duoc.digitalfix.catalog.domain;

import cl.duoc.digitalfix.catalog.error.StockInsuficiente;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/** Repuesto con su stock. Tabla PART. */
@Entity
@Table(name = "PART",
       uniqueConstraints = @UniqueConstraint(name = "UK_PART_SKU",
                                             columnNames = {"COMPANY_ID", "SKU"}))
public class Repuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "COMPANY_ID", nullable = false)
    private Long companyId;

    @Column(name = "SKU", nullable = false, length = 40)
    private String sku;

    @Column(name = "NOMBRE", nullable = false, length = 120)
    private String nombre;

    @Column(name = "STOCK", nullable = false)
    private int stock;

    @Column(name = "STOCK_MINIMO", nullable = false)
    private int stockMinimo;

    @Column(name = "COSTO_UNITARIO", precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "ACTIVO", nullable = false)
    private boolean activo = true;

    @Column(name = "CREADO_EN", nullable = false)
    private Instant creadoEn;

    @Column(name = "ACTUALIZADO_EN")
    private Instant actualizadoEn;

    protected Repuesto() { }

    public Repuesto(Long companyId, String sku, String nombre, int stock, int stockMinimo, BigDecimal costoUnitario) {
        this.companyId = companyId;
        this.sku = sku;
        this.nombre = nombre;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.costoUnitario = costoUnitario;
        this.activo = true;
    }

    @PrePersist
    void alCrear() { this.creadoEn = Instant.now(); }

    @PreUpdate
    void alActualizar() { this.actualizadoEn = Instant.now(); }

    public void actualizar(String nombre, int stock, int stockMinimo, BigDecimal costoUnitario) {
        this.nombre = nombre;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.costoUnitario = costoUnitario;
    }

    /** Descuenta stock. Nunca deja el saldo bajo cero: si no alcanza, falla. */
    public void descontar(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("la cantidad debe ser mayor que cero");
        if (cantidad > this.stock) {
            throw new StockInsuficiente(
                "stock insuficiente para el repuesto " + sku + ": hay " + stock + ", se piden " + cantidad);
        }
        this.stock -= cantidad;
    }

    public void reponer(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("la cantidad debe ser mayor que cero");
        this.stock += cantidad;
    }

    public boolean estaBajoMinimo() { return stock <= stockMinimo; }

    public void desactivar() { this.activo = false; }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public String getSku() { return sku; }
    public String getNombre() { return nombre; }
    public int getStock() { return stock; }
    public int getStockMinimo() { return stockMinimo; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public boolean isActivo() { return activo; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
}
