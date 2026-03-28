package TrackerGym.service;

import TrackerGym.entity.Ejercicio;
import TrackerGym.entity.SerieRealizada;
import TrackerGym.entity.User;
import TrackerGym.repository.EjercicioRepository;
import TrackerGym.repository.SerieRealizadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EjercicioServiceImpl {

    @Autowired
    private EjercicioRepository ejercicioRepository;

    @Autowired
    private SerieRealizadaRepository serieRealizadaRepository;

    public List<Ejercicio> obtenerTodosEjercicios() {
        return ejercicioRepository.findAllByOrderByNombre();
    }

    public List<Ejercicio> obtenerEjerciciosDeUsuario(User user) {
        return ejercicioRepository.findByUserOrUserIsNullOrderByNombre(user);
    }

    public List<Ejercicio> obtenerEjerciciosPorGrupo(String grupoMuscular) {
        return ejercicioRepository.findByGrupoMuscularOrderByNombre(grupoMuscular);
    }

    public Ejercicio obtenerEjercicio(Long id) {
        return ejercicioRepository.findById(id).orElse(null);
    }

    public Ejercicio crearEjercicio(User user, String nombre, String descripcion, String grupoMuscular) {
        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setUser(user);
        ejercicio.setNombre(nombre);
        ejercicio.setDescripcion(descripcion);
        ejercicio.setGrupoMuscular(grupoMuscular);
        return ejercicioRepository.save(ejercicio);
    }

    @Transactional
    public void eliminarEjercicio(Long id) {
        Ejercicio ejercicio = ejercicioRepository.findById(id).orElse(null);
        if (ejercicio != null) {
            // Desligar las series del ejercicio sin borrarlas (preservar historial)
            List<SerieRealizada> series = serieRealizadaRepository.findByEjercicio(ejercicio);
            for (SerieRealizada serie : series) {
                serie.setNombreEjercicio(ejercicio.getNombre());
                serie.setEjercicio(null);
            }
            serieRealizadaRepository.saveAll(series);
            ejercicioRepository.delete(ejercicio);
        }
    }
}

