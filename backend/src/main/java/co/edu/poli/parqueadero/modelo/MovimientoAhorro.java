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
import java.time.LocalDateTime;

@Entity
@Table(name = "MOVIMIENTO_AHORRO")
public class MovimientoAhorro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cuenta", nullable = false)
    private CuentaAhorro cuenta;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "cantidad_horas", nullable = false, precision = 12, scale = 6)
    private BigDecimal cantidadHoras;

    @Column(nullable = false, length = 160)
    private String motivo;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public CuentaAhorro getCuenta() { return cuenta; }
    public void setCuenta(CuentaAhorro cuenta) { this.cuenta = cuenta; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public BigDecimal getCantidadHoras() { return cantidadHoras; }
    public void setCantidadHoras(BigDecimal cantidadHoras) { this.cantidadHoras = cantidadHoras; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}