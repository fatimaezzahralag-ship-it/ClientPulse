package ma.ensam.clientpulse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Métadonnées de la documentation OpenAPI (Swagger UI sur /swagger-ui.html, profil dev).
 *
 * L'API utilise une session HTTP et un jeton CSRF, pas de JWT : pour tester une route depuis Swagger UI,
 * connectez-vous d'abord dans l'application (même navigateur, cookies partagés entre les ports) ;
 * les routes de lecture fonctionnent alors directement.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clientPulseOpenApi() {
        return new OpenAPI().info(new Info()
                .title("ClientPulse API")
                .description("Facturation, suivi des paiements et prévention de la perte de clients.")
                .version("1.0"));
    }
}
