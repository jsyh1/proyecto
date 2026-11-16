package co.edu.poli.parqueadero.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "BENEFICIO")
public class Beneficio {

    public static final String HORAS_GRATIS = "HORAS_GRATIS";
    public static final String PORCENTAJE_DESCUENTO = "PORCENTAJE_DESCUENTO";
    public static final String VALOR_FIJO_DESCUENTO = "VALOR_FIJO_DESCUENTO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_beneficio")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_centro")
    private CentroComercial centro;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(nullable = false)
    private boolean activo;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public CentroComercial getCentro() { return centro; }
    public void setCentro(CentroComercial centro) { this.centro = centro; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
