package TrackerGym.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    /**
     * Mapea la ruta de acceso al formulario de login.
     *
     * Devuelve la vista "login" para que Spring MVC muestre la página de inicio
     * de sesión personalizada del proyecto.
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

}
