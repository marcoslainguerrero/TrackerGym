package TrackerGym.repository;

import TrackerGym.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // Para buscar solo clientes (usuarios que tienen un entrenador asignado o rol USER)
    List<User> findByEntrenadorIsNotNull();

    // Para buscar entrenadores (usuarios con rol ADMIN)
    List<User> findByRoles_Name(String name);
    
    // Para buscar clientes de un entrenador específico
    List<User> findByEntrenador(User entrenador);
}

