package TrackerGym.service;

import TrackerGym.entity.NotificacionCambio;
import TrackerGym.entity.User;
import TrackerGym.repository.NotificacionCambioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionService {
    
    @Autowired
    private NotificacionCambioRepository notificacionRepository;
    
    public NotificacionCambio crearNotificacion(User cliente, User entrenador, String mensaje) {
        NotificacionCambio notificacion = new NotificacionCambio();
        notificacion.setCliente(cliente);
        notificacion.setEntrenador(entrenador);
        notificacion.setMensaje(mensaje);
        notificacion.setFechaCreacion(LocalDateTime.now());
        notificacion.setLeida(false);
        return notificacionRepository.save(notificacion);
    }
    
    public List<NotificacionCambio> obtenerNotificacionesPorCliente(User cliente) {
        return notificacionRepository.findByClienteOrderByFechaCreacionDesc(cliente);
    }
    
    @Transactional
    public void marcarComoLeidas(User cliente) {
        List<NotificacionCambio> noLeidas = notificacionRepository.findByClienteOrderByFechaCreacionDesc(cliente)
                .stream()
                .filter(n -> !n.getLeida())
                .collect(Collectors.toList());
        for (NotificacionCambio n : noLeidas) {
            n.setLeida(true);
        }
        notificacionRepository.saveAll(noLeidas);
    }
    
    public long contarNoLeidas(User cliente) {
        return notificacionRepository.countByClienteAndLeidaFalse(cliente);
    }
}
