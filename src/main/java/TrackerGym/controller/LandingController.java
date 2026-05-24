package TrackerGym.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingController {

    /**
     * Mapea la ruta raíz del sitio ("/") y devuelve la vista "landing".
     *
     * Esta es la página de bienvenida pública del proyecto, accesible sin
     * autenticación. Se usa para mostrar la página de landing antes de que el
     * usuario inicie sesión o se registre.
     */
    @GetMapping("/")
    public String landing() {
        return "landing";
    }
}
