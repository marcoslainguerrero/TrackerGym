package TrackerGym.repository;

import TrackerGym.entity.NotificacionCambio;
import TrackerGym.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionCambioRepository extends JpaRepository<NotificacionCambio, Long> {
    // Notificaciones del cliente ordenadas de más reciente a más antigua
    List<NotificacionCambio> findByClienteOrderByFechaCreacionDesc(User cliente);
    // Contador de notificaciones no leídas; usado por GlobalControllerAdvice para el badge
    long countByClienteAndLeidaFalse(User cliente);
}
