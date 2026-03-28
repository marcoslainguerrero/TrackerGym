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

@Service
public class SerieRealizadaService {

    @Autowired
    private SerieRealizadaRepository serieRealizadaRepository;

    public SerieRealizada crearSerie(User usuario, Ejercicio ejercicio, LocalDate fecha, Integer numeroSerie, 
                                     Integer repeticiones, BigDecimal peso, String notas) {
        SerieRealizada serie = new SerieRealizada();
        serie.setUsuario(usuario);
        serie.setEjercicio(ejercicio);
        serie.setNombreEjercicio(ejercicio.getNombre());
        serie.setFecha(fecha);
        serie.setNumeroSerie(numeroSerie);
        serie.setRepeticiones(repeticiones);
        serie.setPeso(peso);
        serie.setNotas(notas);
        
        return serieRealizadaRepository.save(serie);
    }

    public List<SerieRealizada> obtenerSeriesDelUsuario(User usuario) {
        return serieRealizadaRepository.findByUsuarioOrderByFechaDesc(usuario);
    }

    public List<SerieRealizada> obtenerSeriesPorFecha(User usuario, LocalDate fecha) {
        return serieRealizadaRepository.findByUsuarioAndFechaOrderByEjercicio(usuario, fecha);
    }

    public List<SerieRealizada> obtenerSeriesPorEjercicioYFecha(User usuario, Ejercicio ejercicio, LocalDate fecha) {
        return serieRealizadaRepository.findByUsuarioAndEjercicioAndFechaOrderByNumeroSerie(usuario, ejercicio, fecha);
    }

    public Integer obtenerProxNumeroSerie(User usuario, Ejercicio ejercicio, LocalDate fecha) {
        List<SerieRealizada> series = obtenerSeriesPorEjercicioYFecha(usuario, ejercicio, fecha);
        if (series.isEmpty()) {
            return 1;
        }
        return series.stream().mapToInt(SerieRealizada::getNumeroSerie).max().orElse(0) + 1;
    }

    public void eliminarSerie(Long serieId) {
        serieRealizadaRepository.deleteById(serieId);
    }

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

    public List<SerieRealizada> obtenerSeriesPorRango(User usuario, LocalDate fechaInicio, LocalDate fechaFin) {
        return serieRealizadaRepository.findByUsuarioAndFechaBetweenOrderByFechaDesc(usuario, fechaInicio, fechaFin);
    }
}
