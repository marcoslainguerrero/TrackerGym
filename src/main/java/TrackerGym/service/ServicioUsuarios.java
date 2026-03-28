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

@Service
public class ServicioUsuarios {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
            cliente.get().setEntrenador(entrenador.get());
            return userRepository.save(cliente.get());
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
        } else {
            throw new Exception("Entrenador no encontrado.");
        }
    }
}
