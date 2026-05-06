package TrackerGym.repository;

import TrackerGym.entity.ContratoEntrenador;
import TrackerGym.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContratoEntrenadorRepository extends JpaRepository<ContratoEntrenador, Long> {
    Optional<ContratoEntrenador> findTopByClienteOrderByFechaFinDesc(User cliente);
    
    List<ContratoEntrenador> findByClienteOrderByFechaInicioDesc(User cliente);
    
    List<ContratoEntrenador> findByEntrenadorOrderByFechaInicioDesc(User entrenador);
}
