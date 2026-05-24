package TrackerGym.repository;

import TrackerGym.entity.SerieRealizada;
import TrackerGym.entity.User;
import TrackerGym.entity.Ejercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SerieRealizadaRepository extends JpaRepository<SerieRealizada, Long> {
    // Historial completo del usuario, ordenado por fecha descendente
    List<SerieRealizada> findByUsuarioOrderByFechaDesc(User usuario);

    // Series de un usuario en una fecha concreta (vista sesión de hoy)
    List<SerieRealizada> findByUsuarioAndFechaOrderByEjercicio(User usuario, LocalDate fecha);

    // Series de un usuario para un ejercicio y fecha concretos (para calcular el número de serie)
    List<SerieRealizada> findByUsuarioAndEjercicioAndFechaOrderByNumeroSerie(User usuario, Ejercicio ejercicio,
            LocalDate fecha);

    // Series de un usuario en un rango de fechas (filtros de historial)
    List<SerieRealizada> findByUsuarioAndFechaBetweenOrderByFechaDesc(User usuario, LocalDate fechaInicio,
            LocalDate fechaFin);

    // Utilizado en EjercicioServiceImpl.eliminarEjercicio() para desvincular series antes del borrado
    List<SerieRealizada> findByEjercicio(Ejercicio ejercicio);
}
