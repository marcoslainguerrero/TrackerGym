package TrackerGym.controller;

import TrackerGym.entity.User;
import TrackerGym.entity.Exercise;
import TrackerGym.repository.UserRepository;
import TrackerGym.service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
public class ExerciseController {

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/cliente/agregar-ejercicio")
    public String agregarEjercicio(
            @RequestParam("nombreEjercicio") String nombreEjercicio,
            @RequestParam("fecha") String fecha,
            @RequestParam("series") Integer series,
            @RequestParam("repeticiones") Integer repeticiones,
            @RequestParam(value = "notas", required = false) String notas,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            // Obtener usuario autenticado
            Optional<User> usuario = userRepository.findByUsername(authentication.getName());

            if (usuario.isPresent()) {
                LocalDate fechaParsed = LocalDate.parse(fecha);
                exerciseService.crearEjercicio(usuario.get(), nombreEjercicio, fechaParsed, series, repeticiones,
                        notas);
                redirectAttributes.addFlashAttribute("success", "Ejercicio agregado exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar ejercicio: " + e.getMessage());
        }

        return "redirect:/home";
    }

    @PostMapping("/cliente/eliminar-ejercicio")
    public String eliminarEjercicio(
            @RequestParam("ejercicioId") Long ejercicioId,
            RedirectAttributes redirectAttributes) {

        try {
            exerciseService.eliminarEjercicio(ejercicioId);
            redirectAttributes.addFlashAttribute("success", "Ejercicio eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar ejercicio: " + e.getMessage());
        }

        return "redirect:/home";
    }
}
