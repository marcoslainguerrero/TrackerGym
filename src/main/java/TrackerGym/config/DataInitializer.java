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

        // Crear usuario admin por defecto si no existe
        createAdminUserIfNotExists();
    }

    private void createRoleIfNotExists(String roleName, String description) {
        Optional<Role> existingRole = roleRepository.findByName(roleName);
        if (existingRole.isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            System.out.println("✓ Rol creado: " + roleName);
        } else {
            System.out.println("✓ Rol ya existe: " + roleName);
        }
    }

    private void createAdminUserIfNotExists() {
        Optional<User> existingAdmin = userRepository.findByUsername("entrenador_master");
        if (existingAdmin.isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("entrenador_master");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            
            // Asignar rol ADMIN
            Optional<Role> adminRole = roleRepository.findByName("ROLE_ADMIN");
            if (adminRole.isPresent()) {
                adminUser.setRoles(new HashSet<>());
                adminUser.getRoles().add(adminRole.get());
                userRepository.save(adminUser);
                System.out.println("✓ Usuario administrador creado: entrenador_master");
            }
        } else {
            System.out.println("✓ Usuario administrador ya existe: entrenador_master");
        }
    }
}
