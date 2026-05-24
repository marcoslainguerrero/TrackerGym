package TrackerGym.controller;

import TrackerGym.entity.User;
import TrackerGym.repository.UserRepository;
import TrackerGym.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

/**
 * Intercepta todas las respuestas de los controladores del paquete
 * TrackerGym.controller y añade automáticamente el contador de
 * notificaciones no leídas al modelo. Esto permite que la barra de
 * navegación del cliente muestre el badge sin que cada controlador
 * tenga que calcularlo individualmente.
 */
@ControllerAdvice(basePackages = "TrackerGym.controller")
public class GlobalControllerAdvice {

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private UserRepository userRepository;

    // Se ejecuta antes de renderizar cualquier vista; inyecta el dato global de notificaciones
    @ModelAttribute
    public void addAttributes(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            Optional<User> usuario = userRepository.findByUsername(authentication.getName());
            if (usuario.isPresent()) {
                long notificacionesNoLeidas = notificacionService.contarNoLeidas(usuario.get());
                model.addAttribute("notificacionesNoLeidas", notificacionesNoLeidas);
            }
        }
    }
}
