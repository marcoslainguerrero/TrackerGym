package TrackerGym.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Representa la definición de un ejercicio reutilizable.
 *
 * Ejercicios con user_id = NULL son ejercicios predefinidos del sistema,
 * visibles para todos los usuarios. Los que tienen user_id asignado son
 * ejercicios personalizados creados por ese usuario concreto.
 *
 * La relación con SerieRealizada es OneToMany: un ejercicio puede tener
 * múltiples series registradas. El campo ejercicio_id en SerieRealizada es
 * nullable para preservar el historial cuando el ejercicio es eliminado.
 */
@Entity
@Table(name = "ejercicio")
public class Ejercicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "grupo_muscular")
    private String grupoMuscular;

    // NULL = ejercicio global del sistema; con valor = ejercicio privado del usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "ejercicio")
    private Set<SerieRealizada> series = new HashSet<>();

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getGrupoMuscular() {
        return grupoMuscular;
    }

    public void setGrupoMuscular(String grupoMuscular) {
        this.grupoMuscular = grupoMuscular;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<SerieRealizada> getSeries() {
        return series;
    }

    public void setSeries(Set<SerieRealizada> series) {
        this.series = series;
    }
}
