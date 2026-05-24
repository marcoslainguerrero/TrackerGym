package TrackerGym.security;

import TrackerGym.entity.User;
import TrackerGym.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

/**
 * Servicio de Spring Security que carga los datos de autenticación de un usuario
 * desde la base de datos.
 *
 * Implementa UserDetailsService para que Spring Security pueda obtener el
 * usuario, su contraseña codificada y sus roles durante el proceso de login.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /** Repositorio para buscar el usuario por nombre. */
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Carga los datos del usuario desde la base de datos durante el inicio de sesión.
     * Spring Security utiliza internamente este método para obtener el hash de la
     * contraseña almacenada (BCrypt) y lo compara con la contraseña en texto plano
     * introducida por el usuario, sin necesidad de revertir la encriptación.
     *
     * @param username nombre de usuario a buscar.
     * @return UserDetails con la información de autenticación y roles.
     * @throws UsernameNotFoundException si el usuario no existe.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toList())
        );
    }
}