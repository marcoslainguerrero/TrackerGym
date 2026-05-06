package TrackerGym.controller;

import TrackerGym.entity.Role;
import TrackerGym.entity.User;
import TrackerGym.repository.RoleRepository;
import TrackerGym.repository.UserRepository;
import TrackerGym.service.ServicioUsuarios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;

@Controller
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ServicioUsuarios servicioUsuarios;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("role") String role,
            RedirectAttributes redirectAttributes) {

        logger.info("Intento de registro: usuario={}, rol={}", username, role);

        // Validar que la contraseña no esté vacía
        if (password == null || password.trim().isEmpty()) {
            logger.warn("Contraseña vacía para usuario={}", username);
            redirectAttributes.addAttribute("error", "La contraseña no puede estar vacía.");
            return "redirect:/register";
        }

        // Validar requisitos mínimos de seguridad de la contraseña
        String errorPassword = servicioUsuarios.validarPassword(password);
        if (errorPassword != null) {
            logger.warn("Contraseña no cumple requisitos para usuario={}: {}", username, errorPassword);
            redirectAttributes.addAttribute("error", errorPassword);
            return "redirect:/register";
        }

        // Validar que las contraseñas coincidan
        if (!password.equals(confirmPassword)) {
            logger.warn("Las contraseñas no coinciden para usuario={}", username);
            redirectAttributes.addAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/register";
        }

        // Validar que el usuario no exista
        Optional<User> userExists = userRepository.findByUsername(username);
        if (userExists.isPresent()) {
            logger.warn("Usuario ya existe: {}", username);
            redirectAttributes.addAttribute("error", "El usuario ya existe.");
            return "redirect:/register";
        }

        // Validar que se haya seleccionado un rol
        if (role == null || role.isEmpty()) {
            logger.warn("Rol no seleccionado para usuario={}", username);
            redirectAttributes.addAttribute("error", "Debes seleccionar un tipo de usuario.");
            return "redirect:/register";
        }

        try {
            // Buscar rol en la BD
            Optional<Role> roleObj = roleRepository.findByName(role);
            if (roleObj.isEmpty()) {
                logger.error("El rol no existe en la BD: {}", role);
                redirectAttributes.addAttribute("error", "El rol seleccionado no existe en el sistema.");
                return "redirect:/register";
            }

            // Crear nuevo usuario
            User newUser = new User();
            newUser.setUsername(username);
            String encodedPassword = passwordEncoder.encode(password);
            newUser.setPassword(encodedPassword);
            newUser.setRoles(new HashSet<>());
            newUser.getRoles().add(roleObj.get());

            // Guardar usuario
            userRepository.save(newUser);
            logger.info("Usuario registrado exitosamente: {}", username);

            // Redirigir a login con mensaje de éxito
            redirectAttributes.addAttribute("success", "true");
            return "redirect:/login";

        } catch (Exception e) {
            logger.error("Error al registrar usuario{}: ", username, e);
            redirectAttributes.addAttribute("error", "Error al registrar el usuario: " + e.getMessage());
            return "redirect:/register";
        }
    }
}
