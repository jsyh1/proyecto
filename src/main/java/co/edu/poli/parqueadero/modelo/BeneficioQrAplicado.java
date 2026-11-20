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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "BENEFICIO_QR_APLICADO", uniqueConstraints = {
        @UniqueConstraint(name = "uk_beneficio_qr_clave_hash", columnNames = "clave_compra_hash")
})
public class BeneficioQrAplicado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_beneficio_qr")
    private Integer id;

    @Column(name = "clave_compra_hash", nullable = false, length = 64)
    private String claveCompraHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sesion", nullable = false)
    private SesionParqueadero sesion;

    @Column(name = "fecha_aplicacion", nullable = false)
    private LocalDateTime fechaAplicacion;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getClaveCompraHash() { return claveCompraHash; }
    public void setClaveCompraHash(String claveCompraHash) { this.claveCompraHash = claveCompraHash; }
    public SesionParqueadero getSesion() { return sesion; }
    public void setSesion(SesionParqueadero sesion) { this.sesion = sesion; }
    public LocalDateTime getFechaAplicacion() { return fechaAplicacion; }
    public void setFechaAplicacion(LocalDateTime fechaAplicacion) { this.fechaAplicacion = fechaAplicacion; }
}
