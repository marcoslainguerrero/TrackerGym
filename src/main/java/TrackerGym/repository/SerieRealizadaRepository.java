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
    List<SerieRealizada> findByUsuarioOrderByFechaDesc(User usuario);

    List<SerieRealizada> findByUsuarioAndFechaOrderByEjercicio(User usuario, LocalDate fecha);

    List<SerieRealizada> findByUsuarioAndEjercicioAndFechaOrderByNumeroSerie(User usuario, Ejercicio ejercicio,
            LocalDate fecha);

    List<SerieRealizada> findByUsuarioAndFechaBetweenOrderByFechaDesc(User usuario, LocalDate fechaInicio,
            LocalDate fechaFin);

    List<SerieRealizada> findByEjercicio(Ejercicio ejercicio);
}
