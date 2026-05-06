package TrackerGym.service;

import TrackerGym.entity.Role;
import TrackerGym.entity.User;
import TrackerGym.repository.RoleRepository;
import TrackerGym.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import TrackerGym.entity.ContratoEntrenador;
import TrackerGym.repository.ContratoEntrenadorRepository;
import TrackerGym.service.NotificacionService;

@Service
public class ServicioUsuarios {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ContratoEntrenadorRepository contratoEntrenadorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificacionService notificacionService;

    /**
     * Valida que la contraseña cumpla los requisitos mínimos de seguridad:
     * - Más de 8 caracteres
     * - Al menos una letra mayúscula
     * - Al menos un número
     * - Al menos un carácter especial (!@#$%^&*()-+=[]{};:'",.<>?/\|`~_)
     *
     * @param password contraseña a validar
     * @return mensaje de error si no cumple los requisitos, o null si es válida
     */
    public String validarPassword(String password) {
        if (password == null || password.length() <= 8 ||
            !password.matches(".*[A-Z].*") ||
            !password.matches(".*[0-9].*") ||
            !password.matches(".*[!@#$%^&*()\\-+=\\[\\]{};:'\",.<>?/\\\\|`~_].*")) {
            return "Registro anulado. La contraseña no cumple con los requisitos";
        }
        return null;
    }

    public Optional<ContratoEntrenador> comprobarContratoActivo(User cliente) {
        Optional<ContratoEntrenador> contratoOpt = contratoEntrenadorRepository
                .findTopByClienteOrderByFechaFinDesc(cliente);
        if (contratoOpt.isPresent()) {
            ContratoEntrenador contrato = contratoOpt.get();
            if (contrato.getFechaFin().isBefore(LocalDate.now())) {
                User entrenador = contrato.getEntrenador();
                String msj = "Tu plan de entrenamiento con "
                        + (entrenador != null ? entrenador.getUsername() : "tu entrenador")
                        + " ha expirado y ha sido cancelado automáticamente.";
                try {
                    if (entrenador != null) {
                        notificacionService.crearNotificacion(cliente, entrenador, msj);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                cliente.setEntrenador(null);
                userRepository.save(cliente);
                contratoEntrenadorRepository.delete(contrato);
                return Optional.empty();
            }
            return contratoOpt;
        }
        return Optional.empty();
    }

    public List<User> obtenerClientes() {
        return userRepository.findByEntrenadorIsNotNull();
    }

    public List<User> obtenerEntrenadores() {
        return userRepository.findByRoles_Name("ROLE_ADMIN");
    }

    public User guardarUsuario(User user) {
        return userRepository.save(user);
    }

    public User registrarCliente(String username, String password, Long entrenadorId) throws Exception {
        // Verificar si el usuario ya existe
        if (userRepository.findByUsername(username).isPresent()) {
            throw new Exception("El nombre de usuario '" + username + "' ya existe.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        // Asignar rol USER
        Optional<Role> roleUser = roleRepository.findByName("ROLE_USER");
        if (roleUser.isPresent()) {
            user.setRoles(new HashSet<>());
            user.getRoles().add(roleUser.get());
        }

        // Asignar entrenador si se proporciona
        if (entrenadorId != null) {
            Optional<User> entrenador = userRepository.findById(entrenadorId);
            entrenador.ifPresent(user::setEntrenador);
        }

        return userRepository.save(user);
    }

    public User contratarEntrenador(Long clienteId, Long entrenadorId) {
        Optional<User> cliente = userRepository.findById(clienteId);
        Optional<User> entrenador = userRepository.findById(entrenadorId);

        if (cliente.isPresent() && entrenador.isPresent()) {
            User clienteEntity = cliente.get();
            User entrenadorEntity = entrenador.get();
            clienteEntity.setEntrenador(entrenadorEntity);
            User savedUser = userRepository.save(clienteEntity);

            ContratoEntrenador contrato = new ContratoEntrenador();
            contrato.setCliente(clienteEntity);
            contrato.setEntrenador(entrenadorEntity);
            contrato.setFechaInicio(LocalDate.now());
            contrato.setFechaFin(LocalDate.now().plusMonths(1));
            contratoEntrenadorRepository.save(contrato);

            return savedUser;
        }
        return null;
    }

    public void vincularClienteExistente(String username, Long entrenadorId) throws Exception {
        Optional<User> usuarioOpt = userRepository.findByUsername(username);

        if (usuarioOpt.isEmpty()) {
            throw new Exception("El usuario '" + username + "' no existe.");
        }

        User usuario = usuarioOpt.get();

        // Verificar errores lógicos
        if (usuario.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
            throw new Exception("No puedes vincular a otro entrenador como cliente.");
        }

        if (usuario.getEntrenador() != null) {
            // Opcional: Permitir sobrescribir o lanzar error
            // throw new Exception("El usuario ya tiene un entrenador asignado: " +
            // usuario.getEntrenador().getUsername());
        }

        Optional<User> entrenadorOpt = userRepository.findById(entrenadorId);
        if (entrenadorOpt.isPresent()) {
            usuario.setEntrenador(entrenadorOpt.get());
            userRepository.save(usuario);

            ContratoEntrenador contrato = new ContratoEntrenador();
            contrato.setCliente(usuario);
            contrato.setEntrenador(entrenadorOpt.get());
            contrato.setFechaInicio(LocalDate.now());
            contrato.setFechaFin(LocalDate.now().plusMonths(1));
            contratoEntrenadorRepository.save(contrato);
        } else {
            throw new Exception("Entrenador no encontrado.");
        }
    }
}
