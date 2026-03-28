package TrackerGym.repository;

import TrackerGym.entity.NotificacionCambio;
import TrackerGym.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionCambioRepository extends JpaRepository<NotificacionCambio, Long> {
    List<NotificacionCambio> findByClienteOrderByFechaCreacionDesc(User cliente);
    long countByClienteAndLeidaFalse(User cliente);
}
