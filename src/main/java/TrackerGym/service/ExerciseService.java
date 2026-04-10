package TrackerGym.service;

import TrackerGym.entity.Exercise;
import TrackerGym.entity.User;
import TrackerGym.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExerciseService {

    @Autowired
    private ExerciseRepository exerciseRepository;

    public Exercise crearEjercicio(User usuario, String nombreEjercicio, LocalDate fecha, Integer series,
            Integer repeticiones, String notas) {
        Exercise exercise = new Exercise();
        exercise.setUsuario(usuario);
        exercise.setNombreEjercicio(nombreEjercicio);
        exercise.setFecha(fecha);
        exercise.setSeries(series);
        exercise.setRepeticiones(repeticiones);
        exercise.setNotas(notas);

        return exerciseRepository.save(exercise);
    }

    public List<Exercise> obtenerEjerciciosDelUsuario(User usuario) {
        return exerciseRepository.findByUsuarioOrderByFechaDesc(usuario);
    }

    public void eliminarEjercicio(Long ejercicioId) {
        exerciseRepository.deleteById(ejercicioId);
    }

    public Exercise actualizarEjercicio(Long ejercicioId, String nombreEjercicio, LocalDate fecha, Integer series,
            Integer repeticiones, String notas) {
        Exercise exercise = exerciseRepository.findById(ejercicioId).orElse(null);
        if (exercise != null) {
            exercise.setNombreEjercicio(nombreEjercicio);
            exercise.setFecha(fecha);
            exercise.setSeries(series);
            exercise.setRepeticiones(repeticiones);
            exercise.setNotas(notas);
            return exerciseRepository.save(exercise);
        }
        return null;
    }
}
