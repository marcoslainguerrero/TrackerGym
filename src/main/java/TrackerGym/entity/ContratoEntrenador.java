package TrackerGym.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

/**
 * Formaliza la relación entrenador-cliente con fechas de inicio y fin.
 * Permite controlar la vigencia del servicio de entrenamiento.
 *
 * ServicioUsuarios.comprobarContratoActivo() verifica si la fecha_fin
 * ha pasado y, en caso afirmativo, desvincula automáticamente al cliente
 * de su entrenador y elimina el contrato expirado.
 */
@Entity
@Table(name = "contrato_entrenador")
@Getter
@Setter
public class ContratoEntrenador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private User cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entrenador_id", nullable = false)
    private User entrenador;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCliente() {
        return cliente;
    }

    public void setCliente(User cliente) {
        this.cliente = cliente;
    }

    public User getEntrenador() {
        return entrenador;
    }

    public void setEntrenador(User entrenador) {
        this.entrenador = entrenador;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }
}
