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
@Table(name = "SESION_PARQUEADERO")
public class SesionParqueadero {

    public static final String ESTADO_ABIERTA = "ABIERTA";
    public static final String ESTADO_CERRADA = "CERRADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vehiculo")
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_centro")
    private CentroComercial centro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tarifa")
    private Tarifa tarifa;

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDateTime fechaEntrada;

    @Column(name = "fecha_salida")
    private LocalDateTime fechaSalida;

    @Column(name = "horas_facturables", precision = 5, scale = 2)
    private BigDecimal horasFacturables;

    @Column(name = "horas_gratuitas", precision = 5, scale = 2)
    private BigDecimal horasGratuitas;

    @Column(name = "monto_total", precision = 10, scale = 2)
    private BigDecimal montoTotal;

    private String estado;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }
    public CentroComercial getCentro() { return centro; }
    public void setCentro(CentroComercial centro) { this.centro = centro; }
    public Tarifa getTarifa() { return tarifa; }
    public void setTarifa(Tarifa tarifa) { this.tarifa = tarifa; }
    public LocalDateTime getFechaEntrada() { return fechaEntrada; }
    public void setFechaEntrada(LocalDateTime fechaEntrada) { this.fechaEntrada = fechaEntrada; }
    public LocalDateTime getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDateTime fechaSalida) { this.fechaSalida = fechaSalida; }
    public BigDecimal getHorasFacturables() { return horasFacturables; }
    public void setHorasFacturables(BigDecimal horasFacturables) { this.horasFacturables = horasFacturables; }
    public BigDecimal getHorasGratuitas() { return horasGratuitas; }
    public void setHorasGratuitas(BigDecimal horasGratuitas) { this.horasGratuitas = horasGratuitas; }
    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
