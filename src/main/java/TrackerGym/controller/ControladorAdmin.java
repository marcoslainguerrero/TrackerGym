package TrackerGym.controller;

import TrackerGym.entity.User;
import TrackerGym.entity.SerieRealizada;
import TrackerGym.repository.UserRepository;
import TrackerGym.service.ServicioUsuarios;
import TrackerGym.service.SerieRealizadaService;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import TrackerGym.entity.ContratoEntrenador;
import TrackerGym.repository.ContratoEntrenadorRepository;

@Controller
public class ControladorAdmin {

    @Autowired
    private ServicioUsuarios servicioUsuarios;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SerieRealizadaService serieRealizadaService;

    @Autowired
    private ContratoEntrenadorRepository contratoEntrenadorRepository;

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
                List<SerieRealizada> ejercicios = serieRealizadaService.obtenerSeriesDelUsuario(usuario.get());

                // Calcular estadísticas
                int diasActivos = (int) ejercicios.stream()
                        .map(SerieRealizada::getFecha)
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .count();

                YearMonth mesActual = YearMonth.now();
                long ejerciciosMesActual = ejercicios.stream()
                        .filter(e -> e.getFecha() != null && YearMonth.from(e.getFecha()).equals(mesActual))
                        .count();

                Optional<ContratoEntrenador> contratoOpt = servicioUsuarios.comprobarContratoActivo(usuario.get());
                if (contratoOpt.isPresent()) {
                    ContratoEntrenador contrato = contratoOpt.get();
                    long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), contrato.getFechaFin());
                    model.addAttribute("tienePlan", true);
                    model.addAttribute("diasRestantesPlan", diasRestantes > 0 ? diasRestantes : 0);
                    model.addAttribute("fechaFinPlan", contrato.getFechaFin());
                } else {
                    model.addAttribute("tienePlan", false);
                }

                // Histórico de contrataciones del cliente
                List<ContratoEntrenador> historicoContrataciones = contratoEntrenadorRepository.findByClienteOrderByFechaInicioDesc(usuario.get());
                model.addAttribute("historicoContrataciones", historicoContrataciones);

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
            model.addAttribute("tienePlan", false);
            model.addAttribute("historicoContrataciones", new java.util.ArrayList<>());
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
            List<User> clientes = userRepository.findByEntrenador(entrenadorActivo.get());
            Map<Long, ContratoEntrenador> contratosActivos = new java.util.HashMap<>();
            
            for (User cliente : clientes) {
                Optional<ContratoEntrenador> contratoOpt = servicioUsuarios.comprobarContratoActivo(cliente);
                contratoOpt.ifPresent(c -> contratosActivos.put(cliente.getId(), c));
            }
            
            // Volver a cargar por si alguno fue desvinculado automáticamente
            clientes = userRepository.findByEntrenador(entrenadorActivo.get());
            
            model.addAttribute("clientes", clientes);
            model.addAttribute("contratosActivos", contratosActivos);
        } else {
            model.addAttribute("clientes", new java.util.ArrayList<>());
            model.addAttribute("contratosActivos", new java.util.HashMap<>());
        }

        return "entrenador/lista-clientes";
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
