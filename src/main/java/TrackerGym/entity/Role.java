package TrackerGym.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa un rol de seguridad en el sistema.
 *
 * Los roles se almacenan en la tabla "roles" y se utilizan para controlar
 * el acceso a las rutas del proyecto mediante Spring Security.
 * Ejemplos: ROLE_ADMIN, ROLE_USER.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role {

    /**
     * Identificador único del rol.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del rol, debe ser único.
     * Normalmente incluye el prefijo "ROLE_" para Spring Security.
     */
    @Column(nullable = false, unique = true)
    private String name;

    // Getters y setters explícitos
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
