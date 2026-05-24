package TrackerGym.repository;

import TrackerGym.entity.ContratoEntrenador;
import TrackerGym.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad ContratoEntrenador.
 *
 * Proporciona consultas específicas para obtener contratos de entrenador
 * relacionados con un cliente o un entrenador, ordenados por fechas.
 */
@Repository
public interface ContratoEntrenadorRepository extends JpaRepository<ContratoEntrenador, Long> {

    /**
     * Obtiene el contrato más reciente de un cliente, ordenado por fecha de fin.
     */
    Optional<ContratoEntrenador> findTopByClienteOrderByFechaFinDesc(User cliente);
    
    /**
     * Obtiene todos los contratos de un cliente, ordenados por fecha de inicio descendente.
     */
    List<ContratoEntrenador> findByClienteOrderByFechaInicioDesc(User cliente);
    
    /**
     * Obtiene todos los contratos de un entrenador, ordenados por fecha de inicio descendente.
     */
    List<ContratoEntrenador> findByEntrenadorOrderByFechaInicioDesc(User entrenador);
}
