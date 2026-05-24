package TrackerGym.repository;

import TrackerGym.entity.Ejercicio;
import TrackerGym.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EjercicioRepository extends JpaRepository<Ejercicio, Long> {
    // Ejercicios de un grupo muscular específico, ordenados alfabéticamente
    List<Ejercicio> findByGrupoMuscularOrderByNombre(String grupoMuscular);

    // Todos los ejercicios del sistema, ordenados alfabéticamente
    List<Ejercicio> findAllByOrderByNombre();

    // Ejercicios propios del usuario + ejercicios globales del sistema (user_id = NULL)
    List<Ejercicio> findByUserOrUserIsNullOrderByNombre(User user);

    // Ejercicios propios del usuario filtrados por grupo muscular
    List<Ejercicio> findByUserAndGrupoMuscularOrderByNombre(User user, String grupoMuscular);
}
