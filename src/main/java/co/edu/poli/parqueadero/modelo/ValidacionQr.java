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
@Table(name = "VALIDACION_QR")
public class ValidacionQr {

    public static final String APROBADA = "APROBADA";
    public static final String RECHAZADA = "RECHAZADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_validacion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sesion")
    private SesionParqueadero sesion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_qr")
    private QrBeneficio qr;

    @Column(nullable = false, length = 20)
    private String resultado;

    @Column(length = 200)
    private String motivo;

    @Column(name = "descuento_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal descuentoAplicado = BigDecimal.ZERO;

    @Column(name = "fecha_validacion", nullable = false)
    private LocalDateTime fechaValidacion;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public SesionParqueadero getSesion() { return sesion; }
    public void setSesion(SesionParqueadero sesion) { this.sesion = sesion; }
    public QrBeneficio getQr() { return qr; }
    public void setQr(QrBeneficio qr) { this.qr = qr; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public BigDecimal getDescuentoAplicado() { return descuentoAplicado; }
    public void setDescuentoAplicado(BigDecimal descuentoAplicado) { this.descuentoAplicado = descuentoAplicado; }
    public LocalDateTime getFechaValidacion() { return fechaValidacion; }
    public void setFechaValidacion(LocalDateTime fechaValidacion) { this.fechaValidacion = fechaValidacion; }
}