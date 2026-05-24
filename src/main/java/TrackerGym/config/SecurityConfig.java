package TrackerGym.config;

import TrackerGym.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Componente (bean) encargado de la encriptación de contraseñas.
     * Utiliza el algoritmo BCrypt, que aplica automáticamente un "salt" aleatorio
     * para generar un hash criptográfico unidireccional y seguro. Esto garantiza
     * que el sistema nunca almacene contraseñas en texto plano en la base de datos.
     *
     * @return instancia de BCryptPasswordEncoder.
     */




    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Define las reglas de acceso HTTP de la aplicación:
     * - Rutas públicas: landing, login, register y recursos estáticos.
     * - /entrenador/** requiere ROLE_ADMIN (entrenadores).
     * - /cliente/** requiere ROLE_USER (clientes).
     * - Tras un login exitoso redirige siempre a /home, donde el controlador se
     *   se encarga de reenviar al dashboard correcto según el rol del usuario.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/img/**").permitAll()
                        .requestMatchers("/entrenador/**").hasRole("ADMIN")
                        .requestMatchers("/cliente/**").hasRole("USER")
                        .requestMatchers("/home", "/dashboard").authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll())
                .logout(logout -> logout.permitAll());

        return http.build();
    }

    // Registra el CustomUserDetailsService y el codificador BCrypt como proveedor
    // de autenticación.  Esto es necesario para que Spring Security sepa cómo cargar
    //  los detalles del usuario y cómo verificar las contraseñas durante el proceso de login. 
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http
                .getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());

        return authenticationManagerBuilder.build();
    }
}