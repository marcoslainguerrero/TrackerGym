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

/**
 * Servicio de ejercicios.
 *
 * Centraliza las operaciones de negocio relacionadas con los ejercicios:
 * listado, búsqueda, creación y eliminación segura.
 */
@Service
public class EjercicioServiceImpl {

    @Autowired
    private EjercicioRepository ejercicioRepository;

    @Autowired
    private SerieRealizadaRepository serieRealizadaRepository;

    /**
     * Devuelve todos los ejercicios del sistema ordenados por nombre.
     *
     * Esto incluye tanto ejercicios globales como los ejercicios creados
     * por usuarios, siempre ordenados alfabéticamente.
     */
    public List<Ejercicio> obtenerTodosEjercicios() {
        return ejercicioRepository.findAllByOrderByNombre();
    }

    /**
     * Devuelve los ejercicios del usuario actual junto con los ejercicios
     * globales del sistema (user_id = NULL), ordenados por nombre.
     */
    public List<Ejercicio> obtenerEjerciciosDeUsuario(User user) {
        return ejercicioRepository.findByUserOrUserIsNullOrderByNombre(user);
    }

    /**
     * Devuelve los ejercicios de un grupo muscular específico.
     */
    public List<Ejercicio> obtenerEjerciciosPorGrupo(String grupoMuscular) {
        return ejercicioRepository.findByGrupoMuscularOrderByNombre(grupoMuscular);
    }

    /**
     * Obtiene un ejercicio por su identificador.
     *
     * @return el ejercicio si existe, o null si no se encuentra.
     */
    public Ejercicio obtenerEjercicio(Long id) {
        return ejercicioRepository.findById(id).orElse(null);
    }

    /**
     * Crea un nuevo ejercicio asociado a un usuario.
     *
     * @return el ejercicio guardado.
     */
    public Ejercicio crearEjercicio(User user, String nombre, String descripcion, String grupoMuscular) {
        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setUser(user);
        ejercicio.setNombre(nombre);
        ejercicio.setDescripcion(descripcion);
        ejercicio.setGrupoMuscular(grupoMuscular);
        return ejercicioRepository.save(ejercicio);
    }

    /**
     * Eliminación segura de un ejercicio: antes de borrarlo, desvincula todas
     * las series asociadas poniendo ejercicio_id a NULL y asegurándose de que
     * nombre_ejercicio ya tiene el nombre guardado. Así el historial del usuario
     * permanece intacto aunque el ejercicio deje de existir en la base de datos.
     */
    @Transactional
    public void eliminarEjercicio(Long id) {
        Ejercicio ejercicio = ejercicioRepository.findById(id).orElse(null);
        if (ejercicio != null) {
            List<SerieRealizada> series = serieRealizadaRepository.findByEjercicio(ejercicio);
            for (SerieRealizada serie : series) {
                serie.setNombreEjercicio(ejercicio.getNombre()); // preservar nombre antes de desligar
                serie.setEjercicio(null);                        // romper FK sin borrar la serie
            }
            serieRealizadaRepository.saveAll(series);
            ejercicioRepository.delete(ejercicio);
        }
    }
}
