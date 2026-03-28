package TrackerGym.repository;

import TrackerGym.entity.Exercise;
import TrackerGym.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByUsuarioOrderByFechaDesc(User usuario);
    List<Exercise> findByUsuario(User usuario);
}
