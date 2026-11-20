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
@Table(name = "REGISTRO_ACCESO")
public class RegistroAcceso {

    public static final String EVENTO_ENTRADA = "ENTRADA";
    public static final String EVENTO_SALIDA = "SALIDA";
    public static final String RESULTADO_RECONOCIDO = "RECONOCIDO";
    public static final String RESULTADO_FALLIDO = "FALLIDO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_acceso")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_centro")
    private CentroComercial centro;

    /** Opcional: una detección puede no vincularse a ninguna sesión. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sesion")
    private SesionParqueadero sesion;

    @Column(name = "placa_detectada")
    private String placaDetectada;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @Column(name = "resultado_deteccion", nullable = false)
    private String resultadoDeteccion;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public CentroComercial getCentro() { return centro; }
    public void setCentro(CentroComercial centro) { this.centro = centro; }
    public SesionParqueadero getSesion() { return sesion; }
    public void setSesion(SesionParqueadero sesion) { this.sesion = sesion; }
    public String getPlacaDetectada() { return placaDetectada; }
    public void setPlacaDetectada(String placaDetectada) { this.placaDetectada = placaDetectada; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public String getResultadoDeteccion() { return resultadoDeteccion; }
    public void setResultadoDeteccion(String resultadoDeteccion) { this.resultadoDeteccion = resultadoDeteccion; }
}
