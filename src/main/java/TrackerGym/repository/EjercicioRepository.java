package TrackerGym.repository;

import TrackerGym.entity.Ejercicio;
import TrackerGym.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EjercicioRepository extends JpaRepository<Ejercicio, Long> {
    List<Ejercicio> findByGrupoMuscularOrderByNombre(String grupoMuscular);

    List<Ejercicio> findAllByOrderByNombre();

    List<Ejercicio> findByUserOrUserIsNullOrderByNombre(User user);

    List<Ejercicio> findByUserAndGrupoMuscularOrderByNombre(User user, String grupoMuscular);
}
