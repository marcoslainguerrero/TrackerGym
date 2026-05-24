package TrackerGym.controller;

import TrackerGym.entity.SerieRealizada;
import TrackerGym.entity.User;
import TrackerGym.repository.UserRepository;
import TrackerGym.repository.SerieRealizadaRepository;
import TrackerGym.service.SerieRealizadaService;
import TrackerGym.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import TrackerGym.entity.ContratoEntrenador;
import TrackerGym.repository.ContratoEntrenadorRepository;

/**
 * Controlador del entrenador.
 *
 * Maneja las vistas y las acciones disponibles para los usuarios con rol
 * ROLE_ADMIN, incluyendo el dashboard de entrenador, la vista de cliente, y
 * la edición/eliminación de series realizadas por los clientes.
 */
@Controller
@RequestMapping("/entrenador")
public class TrainerController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SerieRealizadaService serieRealizadaService;

    @Autowired
    private SerieRealizadaRepository serieRealizadaRepository;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private ContratoEntrenadorRepository contratoEntrenadorRepository;

    /**
     * Carga el dashboard del entrenador.
     *
     * Recopila estadísticas de los clientes asignados, incluye total de series,
     * último registro de entrenamiento y días restantes de contrato.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        try {
            // Obtener usuario entrenador actual
            Optional<User> entrenador = userRepository.findByUsername(authentication.getName());

            if (entrenador.isPresent()) {
                // Obtener clientes del entrenador
                List<User> clientes = userRepository.findByEntrenador(entrenador.get());

                // Calcular estadísticas
                int totalSeries = 0;
                Map<Long, Integer> clienteSeries = new HashMap<>();
                Map<Long, LocalDate> ultimoRegistro = new HashMap<>();
                Map<Long, Long> diasRestantes = new HashMap<>();

                for (User cliente : clientes) {
                    List<SerieRealizada> series = serieRealizadaService.obtenerSeriesDelUsuario(cliente);
                    clienteSeries.put(cliente.getId(), series.size());
                    totalSeries += series.size();

                    if (!series.isEmpty()) {
                        ultimoRegistro.put(cliente.getId(), series.get(0).getFecha());
                    }

                    Optional<ContratoEntrenador> contratoOpt = contratoEntrenadorRepository.findTopByClienteOrderByFechaFinDesc(cliente);
                    if (contratoOpt.isPresent()) {
                        long dias = ChronoUnit.DAYS.between(LocalDate.now(), contratoOpt.get().getFechaFin());
                        diasRestantes.put(cliente.getId(), dias > 0 ? dias : 0);
                    }
                }

                model.addAttribute("clientes", clientes);
                model.addAttribute("totalSeries", totalSeries);
                model.addAttribute("clienteSeries", clienteSeries);
                model.addAttribute("ultimoRegistro", ultimoRegistro);
                model.addAttribute("diasRestantes", diasRestantes);

                // Histórico de clientes del entrenador
                List<ContratoEntrenador> historicoClientes = contratoEntrenadorRepository.findByEntrenadorOrderByFechaInicioDesc(entrenador.get());
                model.addAttribute("historicoClientes", historicoClientes);
            }
        } catch (Exception e) {
            model.addAttribute("clientes", new java.util.ArrayList<>());
            model.addAttribute("totalSeries", 0);
            model.addAttribute("clienteSeries", new java.util.HashMap<>());
            model.addAttribute("ultimoRegistro", new java.util.HashMap<>());
            model.addAttribute("historicoClientes", new java.util.ArrayList<>());
        }

        return "entrenador/dashboard";
    }

    /**
     * Muestra el detalle de un cliente específico para el entrenador.
     *
     * Valida que el cliente pertenezca al entrenador autenticado, carga el
     * historial de series por fecha y calcula estadísticas de todos los clientes.
     */
    @GetMapping("/ver-cliente/{clienteId}")
    public String verClienteSeries(
            @PathVariable("clienteId") Long clienteId,
            Model model,
            Authentication authentication) {

        try {
            Optional<User> cliente = userRepository.findById(clienteId);
            Optional<User> entrenador = userRepository.findByUsername(authentication.getName());

            if (cliente.isPresent() && entrenador.isPresent()) {
                // Verificar que el cliente pertenece al entrenador
                if (cliente.get().getEntrenador() == null
                        || !cliente.get().getEntrenador().getId().equals(entrenador.get().getId())) {
                    model.addAttribute("error", "No tienes permiso para ver este cliente");
                    return "entrenador/dashboard";
                }

                // Obtener series del cliente
                List<SerieRealizada> series = serieRealizadaService.obtenerSeriesDelUsuario(cliente.get());

                // Agrupar las series por fecha en orden descendente para mostrar el historial más reciente primero.
                Map<LocalDate, List<SerieRealizada>> seriesPorFecha = new TreeMap<>(Collections.reverseOrder());
                for (SerieRealizada serie : series) {
                    if (serie.getFecha() != null) {
                        seriesPorFecha.computeIfAbsent(serie.getFecha(), k -> new ArrayList<>()).add(serie);
                    }
                }

                // Obtener otros clientes
                List<User> clientes = userRepository.findByEntrenador(entrenador.get());
                int totalSeries = 0;
                Map<Long, Integer> clienteSeries = new HashMap<>();
                Map<Long, LocalDate> ultimoRegistro = new HashMap<>();
                Map<Long, Long> diasRestantes = new HashMap<>();

                for (User c : clientes) {
                    List<SerieRealizada> s = serieRealizadaService.obtenerSeriesDelUsuario(c);
                    clienteSeries.put(c.getId(), s.size());
                    totalSeries += s.size();

                    if (!s.isEmpty()) {
                        ultimoRegistro.put(c.getId(), s.get(0).getFecha());
                    }

                    Optional<ContratoEntrenador> contratoOptOthers = contratoEntrenadorRepository.findTopByClienteOrderByFechaFinDesc(c);
                    if (contratoOptOthers.isPresent()) {
                        long dias = ChronoUnit.DAYS.between(LocalDate.now(), contratoOptOthers.get().getFechaFin());
                        diasRestantes.put(c.getId(), dias > 0 ? dias : 0);
                    }
                }

                // Calcular el promedio de series por sesión del cliente.
                double promedioSeries = seriesPorFecha.isEmpty() ? 0.0 : (double) series.size() / seriesPorFecha.size();

                Optional<ContratoEntrenador> contratoOpt = contratoEntrenadorRepository.findTopByClienteOrderByFechaFinDesc(cliente.get());
                if (contratoOpt.isPresent()) {
                    long diasRestantesMain = ChronoUnit.DAYS.between(LocalDate.now(), contratoOpt.get().getFechaFin());
                    model.addAttribute("tienePlan", true);
                    model.addAttribute("diasRestantesPlan", diasRestantesMain > 0 ? diasRestantesMain : 0);
                } else {
                    model.addAttribute("tienePlan", false);
                }

                model.addAttribute("cliente", cliente.get());
                model.addAttribute("clientes", clientes);
                model.addAttribute("series", series);
                model.addAttribute("seriesPorFecha", seriesPorFecha);
                model.addAttribute("promedioSeries", promedioSeries);
                model.addAttribute("totalSeries", totalSeries);
                model.addAttribute("clienteSeries", clienteSeries);
                model.addAttribute("ultimoRegistro", ultimoRegistro);
                model.addAttribute("diasRestantes", diasRestantes);
            } else {
                model.addAttribute("error", "Cliente no encontrado o acceso denegado");
                return "entrenador/dashboard";
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al cargar el cliente: " + e.getMessage());
            model.addAttribute("error", "Error al cargar los datos del cliente: " + e.getMessage());
            model.addAttribute("cliente", new User());
            model.addAttribute("series", new java.util.ArrayList<>());
            model.addAttribute("seriesPorFecha", new java.util.LinkedHashMap<>());
            model.addAttribute("clientes", new java.util.ArrayList<>());
            model.addAttribute("totalSeries", 0);
            model.addAttribute("clienteSeries", new java.util.HashMap<>());
            return "entrenador/ver-cliente";
        }

        return "entrenador/ver-cliente";
    }

    /**
     * Actualiza una serie del cliente y genera una notificación automática
     * informándole del cambio. Verifica que la serie pertenece a un cliente
     * asignado al entrenador que hace la petición (control de acceso).
     */
    @PostMapping("/editar-serie/{serieId}")
    public String editarSerie(
            @PathVariable("serieId") Long serieId,
            @RequestParam("repeticiones") Integer repeticiones,
            @RequestParam(value = "peso", required = false) String peso,
            @RequestParam(value = "notas", required = false) String notas,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {

        try {
            Optional<SerieRealizada> serie = serieRealizadaRepository.findById(serieId);

            if (serie.isPresent()) {
                // Solo puede editar si el cliente pertenece a este entrenador
                Optional<User> entrenador = userRepository.findByUsername(authentication.getName());
                if (entrenador.isPresent() && serie.get().getUsuario().getEntrenador() != null &&
                        serie.get().getUsuario().getEntrenador().getId().equals(entrenador.get().getId())) {
                    BigDecimal pesoBD = peso != null && !peso.isEmpty() ? new BigDecimal(peso) : null;
                    
                    // Valores antes de actualizar para la notificación
                    Integer repsAnterior = serie.get().getRepeticiones();
                    BigDecimal pesoAnterior = serie.get().getPeso();
                    
                    serieRealizadaService.actualizarSerie(serieId, repeticiones, pesoBD, notas);
                    
                    // Notificar al cliente
                    String nombreEjercicio = serie.get().getNombreEjercicio() != null ? serie.get().getNombreEjercicio() : "Ejercicio eliminado";
                    String msj = "Tu entrenador " + entrenador.get().getUsername() + 
                                 " ha modificado tu serie de " + nombreEjercicio + 
                                 " (Reps: " + repsAnterior + " ➔ " + repeticiones + 
                                 ", Peso: " + (pesoAnterior != null ? pesoAnterior : "-") + "kg ➔ " + (pesoBD != null ? pesoBD : "-") + "kg).";
                    notificacionService.crearNotificacion(serie.get().getUsuario(), entrenador.get(), msj);
                    
                    redirectAttributes.addFlashAttribute("success", "Serie actualizada correctamente.");

                    return "redirect:/entrenador/ver-cliente/" + serie.get().getUsuario().getId();
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la serie");
        }

        return "redirect:/entrenador/dashboard";
    }

    /**
     * Elimina una serie del cliente y genera una notificación automática.
     * Al igual que editarSerie, verifica la autorización del entrenador
     * antes de proceder con el borrado.
     */
    @PostMapping("/eliminar-serie/{serieId}")
    public String eliminarSerie(
            @PathVariable("serieId") Long serieId,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {

        try {
            Optional<SerieRealizada> serie = serieRealizadaRepository.findById(serieId);

            if (serie.isPresent()) {
                Optional<User> entrenador = userRepository.findByUsername(authentication.getName());
                if (entrenador.isPresent() && serie.get().getUsuario().getEntrenador() != null &&
                        serie.get().getUsuario().getEntrenador().getId().equals(entrenador.get().getId())) {
                    Long clienteId = serie.get().getUsuario().getId();
                    String nombreEjercicio = serie.get().getNombreEjercicio() != null ? serie.get().getNombreEjercicio() : "Ejercicio eliminado";
                    User usuarioCliente = serie.get().getUsuario();
                    
                    serieRealizadaService.eliminarSerie(serieId);
                    
                    // Notificar al cliente
                    String msj = "Tu entrenador " + entrenador.get().getUsername() + 
                                 " ha eliminado tu serie de " + nombreEjercicio + 
                                 " del día " + serie.get().getFecha() + ".";
                    notificacionService.crearNotificacion(usuarioCliente, entrenador.get(), msj);
                    
                    redirectAttributes.addFlashAttribute("success", "Serie eliminada correctamente.");

                    return "redirect:/entrenador/ver-cliente/" + clienteId;
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la serie");
        }

        return "redirect:/entrenador/dashboard";
    }
}
