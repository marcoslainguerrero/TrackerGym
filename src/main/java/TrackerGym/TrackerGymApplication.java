package TrackerGym;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Punto de entrada principal de la aplicación Spring Boot.
 *
 * Esta clase arranca el contexto de Spring y lanza el servidor.
 */
@SpringBootApplication
public class TrackerGymApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackerGymApplication.class, args);
    }
}