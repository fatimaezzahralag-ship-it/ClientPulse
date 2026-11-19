package ma.ensam.clientpulse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * TEMPORAIRE, POUR TESTER L'API À LA MAIN : ouvre toutes les routes et coupe le CSRF.
 * Ne s'active QUE avec le profil "sans-securite" :
 *     ./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev,sans-securite"
 * À SUPPRIMER dès que le SecurityConfig de P2 est poussé. Ne jamais l'activer ailleurs que sur son poste.
 */
@Configuration
@Profile("sans-securite")
public class SecuriteTemporaire {

    @Bean
    SecurityFilterChain ouvert(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
