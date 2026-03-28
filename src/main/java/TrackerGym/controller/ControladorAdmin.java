package TrackerGym.controller;

import TrackerGym.entity.User;
import TrackerGym.entity.Exercise;
import TrackerGym.repository.UserRepository;
import TrackerGym.service.ServicioUsuarios;
import TrackerGym.service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.HashSet;

@Controller
public class ControladorAdmin {

    @Autowired
    private ServicioUsuarios servicioUsuarios;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseService exerciseService;

    /**
     * Gestiona la redirección inicial tras el login exitoso.
     * Si el usuario es ADMIN (Entrenador), lo envía a su panel de entrenador.
     * Si el usuario es USER (Cliente), lo envía a su panel de cliente.
     */
    @GetMapping("/home")
    public String home(Authentication auth, Model model) {
        // Verificamos si el usuario tiene el rol de administrador (Entrenador)
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return "redirect:/entrenador/dashboard"; // Panel de entrenador
        }

        // Panel de cliente - obtener sus ejercicios
        try {
            Optional<User> usuario = userRepository.findByUsername(auth.getName());
            if (usuario.isPresent()) {
                List<Exercise> ejercicios = exerciseService.obtenerEjerciciosDelUsuario(usuario.get());

                // Calcular estadísticas
                int diasActivos = (int) ejercicios.stream()
                        .map(Exercise::getFecha)
                        .distinct()
                        .count();

                YearMonth mesActual = YearMonth.now();
                long ejerciciosMesActual = ejercicios.stream()
                        .filter(e -> YearMonth.from(e.getFecha()).equals(mesActual))
                        .count();

                model.addAttribute("ejercicios", ejercicios);
                model.addAttribute("ejercicioPorFecha", ejercicios);
                model.addAttribute("diasActivos", diasActivos);
                model.addAttribute("ejerciciosMesActual", ejerciciosMesActual);
            }
        } catch (Exception e) {
            // Si hay error al obtener ejercicios, simplemente continuamos sin ellos
            model.addAttribute("ejercicios", new java.util.ArrayList<>());
            model.addAttribute("ejercicioPorFecha", new java.util.ArrayList<>());
            model.addAttribute("diasActivos", 0);
            model.addAttribute("ejerciciosMesActual", 0);
        }

        return "cliente/dashboard"; // Panel de cliente
    }

    /**
     * Muestra la lista de clientes registrados en el sistema.
     */
    @GetMapping("/entrenador/clientes")
    public String listarClientes(Authentication auth, Model model) {
        Optional<User> entrenadorActivo = userRepository.findByUsername(auth.getName());
        
        if (entrenadorActivo.isPresent()) {
            // Buscamos únicamente los clientes que pertenezcan a este entrenador
            List<User> clientes = userRepository.findByEntrenador(entrenadorActivo.get());
            model.addAttribute("clientes", clientes);
        } else {
            model.addAttribute("clientes", new java.util.ArrayList<>());
        }

        return "entrenador/lista-clientes"; // Retorna templates/entrenador/lista-clientes.html
    }

    /**
     * Muestra el formulario para registrar un nuevo cliente.
     */
    @GetMapping("/entrenador/clientes/nuevo")
    public String mostrarFormularioRegistro(Model model) {
        List<User> entrenadores = servicioUsuarios.obtenerEntrenadores();
        model.addAttribute("entrenadores", entrenadores);
        return "entrenador/registrar-cliente";
    }

    /**
     * Procesa el registro de un nuevo cliente.
     */
    @PostMapping("/entrenador/clientes/nuevo")
    public String registrarCliente(@RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam(value = "entrenadorId", required = false) Long entrenadorId,
            RedirectAttributes redirectAttributes) {
        try {
            servicioUsuarios.registrarCliente(username, password, entrenadorId);
            redirectAttributes.addFlashAttribute("success", "Cliente registrado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar el cliente: " + e.getMessage());
        }
        return "redirect:/entrenador/clientes";
    }

    @PostMapping("/entrenador/clientes/vincular")
    public String vincularCliente(@RequestParam("username") String username,
            @RequestParam("entrenadorId") Long entrenadorId,
            RedirectAttributes redirectAttributes) {
        try {
            servicioUsuarios.vincularClienteExistente(username, entrenadorId);
            redirectAttributes.addFlashAttribute("success", "Cliente vinculado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al vincular el cliente: " + e.getMessage());
        }
        return "redirect:/entrenador/clientes";
    }
}
