package TrackerGym.service;

import TrackerGym.entity.SerieRealizada;
import TrackerGym.entity.User;
import TrackerGym.entity.Ejercicio;
import TrackerGym.repository.SerieRealizadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio que gestiona las series realizadas por los usuarios.
 *
 * Centraliza la lógica de negocio para crear, listar, actualizar y eliminar
 * series, así como para calcular el siguiente número de serie en una sesión.
 */
@Service
public class SerieRealizadaService {

    /** Repositorio para acceder a las series realizadas en la base de datos. */
    @Autowired
    private SerieRealizadaRepository serieRealizadaRepository;

    /**
     * Persiste una nueva serie realizada. Se guarda el nombre del ejercicio
     * como texto (setNombreEjercicio) además de la FK, de forma que si el
     * ejercicio original se borra en el futuro, el historial no pierde el nombre.
     */
    public SerieRealizada crearSerie(User usuario, Ejercicio ejercicio, LocalDate fecha, Integer numeroSerie,
                                     Integer repeticiones, BigDecimal peso, String notas) {
        SerieRealizada serie = new SerieRealizada();
        serie.setUsuario(usuario);
        serie.setEjercicio(ejercicio);
        // Copia defensiva del nombre para preservar el historial ante futuras eliminaciones
        serie.setNombreEjercicio(ejercicio.getNombre());
        serie.setFecha(fecha);
        serie.setNumeroSerie(numeroSerie);
        serie.setRepeticiones(repeticiones);
        serie.setPeso(peso);
        serie.setNotas(notas);

        return serieRealizadaRepository.save(serie);
    }

    /**
     * Devuelve todas las series de un usuario ordenadas por fecha descendente.
     *
     * Se utiliza para mostrar el historial completo de entrenamiento del cliente.
     */
    public List<SerieRealizada> obtenerSeriesDelUsuario(User usuario) {
        return serieRealizadaRepository.findByUsuarioOrderByFechaDesc(usuario);
    }

    /**
     * Devuelve las series de un usuario para una fecha concreta, ordenadas por ejercicio.
     *
     * Utilizado para la vista de sesión diaria o para agrupar la actividad de un día.
     */
    public List<SerieRealizada> obtenerSeriesPorFecha(User usuario, LocalDate fecha) {
        return serieRealizadaRepository.findByUsuarioAndFechaOrderByEjercicio(usuario, fecha);
    }

    /**
     * Devuelve las series de un usuario para un ejercicio y fecha específicos,
     * ordenadas por número de serie.
     */
    public List<SerieRealizada> obtenerSeriesPorEjercicioYFecha(User usuario, Ejercicio ejercicio, LocalDate fecha) {
        return serieRealizadaRepository.findByUsuarioAndEjercicioAndFechaOrderByNumeroSerie(usuario, ejercicio, fecha);
    }

    /**
     * Calcula el número de la siguiente serie para un ejercicio en una fecha concreta.
     * Si es la primera serie del día devuelve 1, si no devuelve el máximo existente + 1.
     * Se usa en ClienteEjercicioController antes de guardar cada serie nueva.
     */
    public Integer obtenerProxNumeroSerie(User usuario, Ejercicio ejercicio, LocalDate fecha) {
        List<SerieRealizada> series = obtenerSeriesPorEjercicioYFecha(usuario, ejercicio, fecha);
        if (series.isEmpty()) {
            return 1;
        }
        return series.stream().mapToInt(SerieRealizada::getNumeroSerie).max().orElse(0) + 1;
    }

    /**
     * Elimina una serie existente por su ID.
     *
     * Se usa cuando el entrenador borra una serie desde su panel de cliente.
     */
    public void eliminarSerie(Long serieId) {
        serieRealizadaRepository.deleteById(serieId);
    }

    /**
     * Actualiza los datos de una serie existente y la guarda.
     *
     * Retorna la serie actualizada o null si no se encuentra.
     */
    public SerieRealizada actualizarSerie(Long serieId, Integer repeticiones, BigDecimal peso, String notas) {
        SerieRealizada serie = serieRealizadaRepository.findById(serieId).orElse(null);
        if (serie != null) {
            serie.setRepeticiones(repeticiones);
            serie.setPeso(peso);
            serie.setNotas(notas);
            return serieRealizadaRepository.save(serie);
        }
        return null;
    }

    /**
     * Devuelve las series de un usuario en un rango de fechas, ordenadas por fecha descendente.
     *
     * Esta consulta se usa para filtrar el historial de entrenamiento entre fechas.
     */
    public List<SerieRealizada> obtenerSeriesPorRango(User usuario, LocalDate fechaInicio, LocalDate fechaFin) {
        return serieRealizadaRepository.findByUsuarioAndFechaBetweenOrderByFechaDesc(usuario, fechaInicio, fechaFin);
    }
}
