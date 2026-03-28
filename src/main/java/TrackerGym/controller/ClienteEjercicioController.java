package TrackerGym.controller;

import TrackerGym.entity.Ejercicio;
import TrackerGym.entity.SerieRealizada;
import TrackerGym.entity.User;
import TrackerGym.repository.UserRepository;
import TrackerGym.service.EjercicioServiceImpl;
import TrackerGym.service.SerieRealizadaService;
import TrackerGym.service.ServicioUsuarios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import TrackerGym.entity.NotificacionCambio;
import TrackerGym.service.NotificacionService;
import TrackerGym.repository.SerieRealizadaRepository;
import TrackerGym.repository.EjercicioRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cliente")
public class ClienteEjercicioController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EjercicioServiceImpl ejercicioService;

    @Autowired
    private SerieRealizadaService serieRealizadaService;

    @Autowired
    private ServicioUsuarios servicioUsuarios;
    
    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private SerieRealizadaRepository serieRealizadaRepository;

    @Autowired
    private EjercicioRepository ejercicioRepository;

    @GetMapping("/ejercicios")
    public String listarEjercicios(Model model, Authentication authentication) {
        try {
            Optional<User> usuario = userRepository.findByUsername(authentication.getName());
            if (usuario.isPresent()) {
                List<Ejercicio> ejercicios = ejercicioService.obtenerEjerciciosDeUsuario(usuario.get());
                model.addAttribute("ejercicios", ejercicios);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al listar ejercicios: " + e.getMessage());
            model.addAttribute("ejercicios", new java.util.ArrayList<>());
            model.addAttribute("error", "Error al cargar ejercicios: " + e.getMessage());
        }
        return "cliente/ejercicios";
    }

    @GetMapping("/agregar-serie")
    public String mostrarFormularioSerie(@RequestParam(value = "ejercicioId", required = false) Long ejercicioId,
            Model model, Authentication authentication) {
        model.addAttribute("ejercicios", new java.util.ArrayList<>());
        try {
            Optional<User> usuario = userRepository.findByUsername(authentication.getName());

            if (usuario.isPresent()) {
                List<Ejercicio> ejercicios = ejercicioService.obtenerEjerciciosDeUsuario(usuario.get());
                model.addAttribute("ejercicios", ejercicios);

                if (ejercicioId != null) {
                    Ejercicio ejercicio = ejercicioService.obtenerEjercicio(ejercicioId);
                    if (ejercicio != null) {
                        LocalDate hoy = LocalDate.now();
                        Integer proxNumeroSerie = serieRealizadaService.obtenerProxNumeroSerie(usuario.get(), ejercicio,
                                hoy);
                        model.addAttribute("ejercicio", ejercicio);
                        model.addAttribute("proxNumeroSerie", proxNumeroSerie);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar los datos: " + e.getMessage());
        }
        return "cliente/agregar-serie";
    }

    @PostMapping("/guardar-serie")
    public String guardarSerie(
            @RequestParam("ejercicioId") Long ejercicioId,
            @RequestParam("repeticiones") Integer repeticiones,
            @RequestParam(value = "peso", required = false) String peso,
            @RequestParam(value = "notas", required = false) String notas,
            @RequestParam(value = "fecha", required = false) String fecha,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<User> usuario = userRepository.findByUsername(authentication.getName());
            Ejercicio ejercicio = ejercicioService.obtenerEjercicio(ejercicioId);

            if (usuario.isPresent() && ejercicio != null) {
                LocalDate fechaSerie;
                if (fecha != null && !fecha.isEmpty()) {
                    fechaSerie = LocalDate.parse(fecha);
                } else {
                    fechaSerie = LocalDate.now();
                }

                Integer numeroSerie = serieRealizadaService.obtenerProxNumeroSerie(usuario.get(), ejercicio,
                        fechaSerie);
                BigDecimal pesoBD = peso != null && !peso.isEmpty() ? new BigDecimal(peso) : null;

                serieRealizadaService.crearSerie(usuario.get(), ejercicio, fechaSerie, numeroSerie, repeticiones,
                        pesoBD, notas);
                redirectAttributes.addFlashAttribute("success", "Serie registrada correctamente");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar la serie: " + e.getMessage());
        }

        return "redirect:/cliente/historial";
    }

    @GetMapping("/historial")
    public String verHistorial(Model model, Authentication authentication) {
        try {
            Optional<User> usuario = userRepository.findByUsername(authentication.getName());

            if (usuario.isPresent()) {
                List<SerieRealizada> series = serieRealizadaService.obtenerSeriesDelUsuario(usuario.get());
                model.addAttribute("series", series);

                // Agrupar por fecha
                java.util.Map<LocalDate, List<SerieRealizada>> seriesPorFecha = new java.util.LinkedHashMap<>();
                for (SerieRealizada serie : series) {
                    seriesPorFecha.computeIfAbsent(serie.getFecha(), k -> new java.util.ArrayList<>()).add(serie);
                }
                model.addAttribute("seriesPorFecha", seriesPorFecha);
            }
        } catch (Exception e) {
            model.addAttribute("series", new java.util.ArrayList<>());
            model.addAttribute("seriesPorFecha", new java.util.HashMap<>());
        }
        return "cliente/historial";
    }

    @GetMapping("/sesion-hoy")
    public String verEjerciciosHoy(Model model, Authentication authentication) {
        try {
            Optional<User> usuario = userRepository.findByUsername(authentication.getName());

            if (usuario.isPresent()) {
                LocalDate hoy = LocalDate.now();
                List<SerieRealizada> series = serieRealizadaService.obtenerSeriesPorFecha(usuario.get(), hoy);

                // Agrupar por nombre de ejercicio
                java.util.Map<String, List<SerieRealizada>> seriesPorEjercicio = new java.util.LinkedHashMap<>();
                for (SerieRealizada serie : series) {
                    String nombre = serie.getNombreEjercicio() != null ? serie.getNombreEjercicio() : "Ejercicio eliminado";
                    seriesPorEjercicio.computeIfAbsent(nombre, k -> new java.util.ArrayList<>())
                            .add(serie);
                }
                model.addAttribute("seriesPorEjercicio", seriesPorEjercicio);
            }
        } catch (Exception e) {
            model.addAttribute("seriesPorEjercicio", new java.util.HashMap<>());
        }
        return "cliente/sesion-hoy";
    }

    @PostMapping("/eliminar-serie/{serieId}")
    public String eliminarSerie(@PathVariable("serieId") Long serieId, RedirectAttributes redirectAttributes) {
        try {
            serieRealizadaService.eliminarSerie(serieId);
            redirectAttributes.addFlashAttribute("success", "Serie eliminada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la serie");
        }
        return "redirect:/cliente/historial";
    }

    @GetMapping("/anadir-ejercicio")
    public String mostrarFormularioAnadirEjercicio(Model model) {
        return "cliente/anadir-ejercicio";
    }

    @PostMapping("/guardar-ejercicio")
    public String guardarEjercicio(
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("grupoMuscular") String grupoMuscular,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<User> usuario = userRepository.findByUsername(authentication.getName());
            if (usuario.isPresent()) {
                ejercicioService.crearEjercicio(usuario.get(), nombre, descripcion, grupoMuscular);
                redirectAttributes.addFlashAttribute("success", "Ejercicio añadido correctamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el ejercicio: " + e.getMessage());
        }

        return "redirect:/cliente/ejercicios";
    }

    @PostMapping("/eliminar-ejercicio/{ejercicioId}")
    public String eliminarEjercicio(@PathVariable("ejercicioId") Long ejercicioId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<User> usuario = userRepository.findByUsername(authentication.getName());
            Ejercicio ejercicio = ejercicioService.obtenerEjercicio(ejercicioId);
            
            if (usuario.isPresent() && ejercicio != null) {
                if (ejercicio.getUser() != null && ejercicio.getUser().getId().equals(usuario.get().getId())) {
                    ejercicioService.eliminarEjercicio(ejercicioId);
                    redirectAttributes.addFlashAttribute("success", "Ejercicio eliminado correctamente");
                } else {
                    redirectAttributes.addFlashAttribute("error", "No puedes eliminar ejercicios predefinidos del sistema");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Error al eliminar el ejercicio: Ejercicio no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el ejercicio");
        }
        return "redirect:/cliente/ejercicios";
    }

    @GetMapping("/contratar-entrenador")
    public String mostrarFormularioContratarEntrenador(Model model, Authentication authentication) {
        try {
            List<User> entrenadores = servicioUsuarios.obtenerEntrenadores();
            model.addAttribute("entrenadores", entrenadores);

            Optional<User> usuario = userRepository.findByUsername(authentication.getName());
            if (usuario.isPresent()) {
                model.addAttribute("entrenadorActual", usuario.get().getEntrenador());
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los entrenadores");
        }
        return "cliente/contratar-entrenador";
    }

    @PostMapping("/guardar-contratacion")
    public String guardarContratacion(
            @RequestParam("entrenadorId") Long entrenadorId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<User> cliente = userRepository.findByUsername(authentication.getName());

            if (cliente.isPresent()) {
                servicioUsuarios.contratarEntrenador(cliente.get().getId(), entrenadorId);
                redirectAttributes.addFlashAttribute("success", "Entrenador contratado correctamente");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al contratar entrenador: " + e.getMessage());
        }

        return "redirect:/cliente/ejercicios";
    }

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
            Optional<User> usuario = userRepository.findByUsername(authentication.getName());

            if (serie.isPresent() && usuario.isPresent()) {
                if (serie.get().getUsuario().getId().equals(usuario.get().getId())) {
                    BigDecimal pesoBD = peso != null && !peso.isEmpty() ? new BigDecimal(peso) : null;
                    serieRealizadaService.actualizarSerie(serieId, repeticiones, pesoBD, notas);
                    redirectAttributes.addFlashAttribute("success", "Serie actualizada correctamente");
                } else {
                    redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar esta serie");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la serie");
        }
        // Redirect back to the referrer (historial or sesion-hoy)
        return "redirect:/cliente/historial";
    }

    @GetMapping("/editar-ejercicio/{id}")
    public String mostrarFormularioEditarEjercicio(@PathVariable("id") Long id, Model model, Authentication authentication) {
        try {
            Optional<User> usuario = userRepository.findByUsername(authentication.getName());
            Ejercicio ejercicio = ejercicioService.obtenerEjercicio(id);

            if (usuario.isPresent() && ejercicio != null) {
                if (ejercicio.getUser() != null && ejercicio.getUser().getId().equals(usuario.get().getId())) {
                    model.addAttribute("ejercicio", ejercicio);
                    return "cliente/editar-ejercicio";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/cliente/ejercicios";
    }

    @PostMapping("/guardar-ejercicio-editado/{id}")
    public String guardarEjercicioEditado(
            @PathVariable("id") Long id,
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("grupoMuscular") String grupoMuscular,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<User> usuario = userRepository.findByUsername(authentication.getName());
            Ejercicio ejercicio = ejercicioService.obtenerEjercicio(id);

            if (usuario.isPresent() && ejercicio != null) {
                if (ejercicio.getUser() != null && ejercicio.getUser().getId().equals(usuario.get().getId())) {
                    ejercicio.setNombre(nombre);
                    ejercicio.setDescripcion(descripcion);
                    ejercicio.setGrupoMuscular(grupoMuscular);
                    ejercicioRepository.save(ejercicio);
                    redirectAttributes.addFlashAttribute("success", "Ejercicio actualizado correctamente");
                } else {
                    redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar este ejercicio");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el ejercicio");
        }
        return "redirect:/cliente/ejercicios";
    }

    @GetMapping("/notificaciones")
    public String verNotificaciones(Model model, Authentication authentication) {
        try {
            Optional<User> usuario = userRepository.findByUsername(authentication.getName());
            if (usuario.isPresent()) {
                List<NotificacionCambio> notificaciones = notificacionService.obtenerNotificacionesPorCliente(usuario.get());
                model.addAttribute("notificaciones", notificaciones);
                
                // Marcar como leídas al visitar la página
                notificacionService.marcarComoLeidas(usuario.get());
            }
        } catch (Exception e) {
            model.addAttribute("notificaciones", new java.util.ArrayList<>());
        }
        return "cliente/notificaciones";
    }
}
