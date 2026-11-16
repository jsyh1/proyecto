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
@Table(name = "PAGO")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sesion", nullable = false, unique = true)
    private SesionParqueadero sesion;

    @Column(nullable = false, length = 30)
    private String metodo;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public SesionParqueadero getSesion() { return sesion; }
    public void setSesion(SesionParqueadero sesion) { this.sesion = sesion; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
}