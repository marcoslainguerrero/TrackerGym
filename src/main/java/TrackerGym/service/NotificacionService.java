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

/**
 * Gestiona el ciclo de vida de las notificaciones entre entrenador y cliente.
 * Se invoca desde TrainerController cuando el entrenador modifica o elimina
 * una serie, y desde ServicioUsuarios cuando expira un contrato.
 * GlobalControllerAdvice llama a contarNoLeidas() en cada petición para
 * mostrar el badge de notificaciones en la barra de navegación del cliente.
 */
@Service
public class NotificacionService {

    @Autowired
    private NotificacionCambioRepository notificacionRepository;
    
    /**
     * Crea y persiste una notificación de cambio para un cliente.
     *
     * Esta notificación se usa para informar al cliente cuando el entrenador
     * modifica o elimina una serie, o cuando se produce algún otro evento relevante.
     */
    public NotificacionCambio crearNotificacion(User cliente, User entrenador, String mensaje) {
        NotificacionCambio notificacion = new NotificacionCambio();
        notificacion.setCliente(cliente);
        notificacion.setEntrenador(entrenador);
        notificacion.setMensaje(mensaje);
        notificacion.setFechaCreacion(LocalDateTime.now());
        notificacion.setLeida(false);
        return notificacionRepository.save(notificacion);
    }
    
    /**
     * Devuelve el historial de notificaciones de un cliente, ordenado desde la
     * más reciente a la más antigua.
     */
    public List<NotificacionCambio> obtenerNotificacionesPorCliente(User cliente) {
        return notificacionRepository.findByClienteOrderByFechaCreacionDesc(cliente);
    }
    
    /**
     * Marca todas las notificaciones no leídas de un cliente como leídas.
     *
     * Se utiliza típicamente cuando el cliente abre su panel de notificaciones
     * o cuando se quiere limpiar el contador de notificaciones nuevas.
     */
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
    
    /**
     * Cuenta las notificaciones no leídas de un cliente.
     *
     * Esto se usa para mostrar el badge numérico de notificaciones pendientes en
     * la interfaz de usuario.
     */
    public long contarNoLeidas(User cliente) {
        return notificacionRepository.countByClienteAndLeidaFalse(cliente);
    }
}
