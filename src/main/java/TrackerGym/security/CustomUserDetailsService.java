package TrackerGym.security;

import TrackerGym.entity.User;
import TrackerGym.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Log para depuración
        System.out.println("DEBUG: Intentando autenticar a: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado en DB"));

                System.out.println("USUARIO ENCONTRADO: " + user.getUsername());
    System.out.println("HASH EN DB: [" + user.getPassword() + "]");

        System.out.println("DEBUG: Usuario encontrado. Hash en DB: " + user.getPassword());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName())) // Debe ser ROLE_ADMIN
                    .collect(Collectors.toList())
        );
    }
}