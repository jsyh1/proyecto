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
import java.time.LocalDateTime;

@Entity
@Table(name = "QR_BENEFICIO")
public class QrBeneficio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_qr")
    private Integer id;

    @Column(name = "codigo_hash", nullable = false, unique = true, length = 64)
    private String codigoHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_beneficio")
    private Beneficio beneficio;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDateTime fechaVencimiento;

    @Column(nullable = false)
    private boolean consumido;

    @Column(name = "fecha_consumo")
    private LocalDateTime fechaConsumo;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCodigoHash() { return codigoHash; }
    public void setCodigoHash(String codigoHash) { this.codigoHash = codigoHash; }
    public Beneficio getBeneficio() { return beneficio; }
    public void setBeneficio(Beneficio beneficio) { this.beneficio = beneficio; }
    public LocalDateTime getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDateTime fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public boolean isConsumido() { return consumido; }
    public void setConsumido(boolean consumido) { this.consumido = consumido; }
    public LocalDateTime getFechaConsumo() { return fechaConsumo; }
    public void setFechaConsumo(LocalDateTime fechaConsumo) { this.fechaConsumo = fechaConsumo; }
}