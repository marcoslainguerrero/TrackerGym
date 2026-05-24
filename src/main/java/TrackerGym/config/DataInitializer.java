package TrackerGym.config;

import TrackerGym.entity.Role;
import TrackerGym.entity.User;
import TrackerGym.repository.RoleRepository;
import TrackerGym.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;

/**
 * Se ejecuta automáticamente al arrancar la aplicación (CommandLineRunner).
 * Garantiza que los roles ROLE_ADMIN y ROLE_USER existan en la base de datos
 * y que haya al menos un usuario entrenador por defecto para poder acceder
 * al sistema desde el primer despliegue.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, 
                          PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Crear roles si no existen
        createRoleIfNotExists("ROLE_ADMIN", "Administrador/Entrenador");
        createRoleIfNotExists("ROLE_USER", "Cliente");

        
    }

    private void createRoleIfNotExists(String roleName, String description) {
        Optional<Role> existingRole = roleRepository.findByName(roleName);
        if (existingRole.isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            System.out.println("Rol creado: " + roleName);
        } else {
            System.out.println("Rol ya existe: " + roleName);
        }
    }
}

