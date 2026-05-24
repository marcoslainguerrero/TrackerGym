package TrackerGym.repository;

import TrackerGym.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio para la entidad Role.
 *
 * Permite realizar operaciones CRUD sobre los roles de seguridad y buscar
 * un rol por su nombre único.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Busca un rol por su nombre único.
     *
     * @param name Nombre del rol (ej. ROLE_ADMIN, ROLE_USER)
     * @return Rol encontrado si existe
     */
    Optional<Role> findByName(String name);
}
