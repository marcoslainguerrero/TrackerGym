package TrackerGym.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad central del sistema. Representa tanto a clientes (ROLE_USER) como
 * a entrenadores (ROLE_ADMIN) en la misma tabla "usuarios".
 *
 * La relación entrenador_id es una auto-referencia: un usuario (cliente)
 * apunta al usuario (entrenador) que lo supervisa. Si entrenador_id es NULL,
 * el cliente no tiene entrenador asignado.
 *
 * La tabla intermedia "usuarios_roles" gestiona la relación muchos-a-muchos
 * con roles, siguiendo el estándar que espera Spring Security.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    // Contraseña almacenada como hash BCrypt, nunca en texto plano
    @Column(nullable = false)
    private String password;

    // Cargado EAGER para que Spring Security pueda verificar roles en cada petición
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuarios_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // Auto-referencia: cliente → entrenador (NULL si no tiene entrenador asignado)
    @ManyToOne
    @JoinColumn(name = "entrenador_id")
    private User entrenador;

    // Explicit getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public User getEntrenador() {
        return entrenador;
    }

    public void setEntrenador(User entrenador) {
        this.entrenador = entrenador;
    }
}
