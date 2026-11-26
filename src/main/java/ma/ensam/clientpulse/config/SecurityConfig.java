package ma.ensam.clientpulse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository) throws Exception {

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(
                                CookieCsrfTokenRepository.withHttpOnlyFalse()
                        )
                        .csrfTokenRequestHandler(
                                new CsrfTokenRequestAttributeHandler()
                        )
                )

                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository)
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/csrf"
                        ).permitAll()
                        .requestMatchers("/api/utilisateurs/**")
                        .hasRole("ADMIN")

                        // Modification des paramètres : ADMIN uniquement.
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/parametres",
                                "/api/parametres/**"
                        ).hasRole("ADMIN")

                        // Recalcul manuel : ADMIN et MANAGER seulement.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/analytics/recalculer"
                        ).hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers("/api/analytics/**")
                        .hasAnyRole("ADMIN", "MANAGER", "ANALYSTE")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/parametres",
                                "/api/parametres/**"
                        ).hasAnyRole("ADMIN", "MANAGER", "ANALYSTE")

                        // Import CSV : ADMIN uniquement (cas d'utilisation « Importer des données CSV »).
                        .requestMatchers("/api/import/**")
                        .hasRole("ADMIN")

                        // Tableau de bord financier et son export CSV.
                        .requestMatchers("/api/dashboard/**")
                        .hasAnyRole("ADMIN", "MANAGER", "ANALYSTE")

                        // PDF : le comptable édite les factures, les commerciaux n'impriment que leurs devis.
                        .requestMatchers(HttpMethod.GET, "/api/factures/*/pdf")
                        .hasAnyRole("ADMIN", "MANAGER", "COMPTABLE")
                        .requestMatchers(HttpMethod.GET, "/api/devis/*/pdf")
                        .hasAnyRole("ADMIN", "MANAGER", "COMMERCIAL", "COMPTABLE")

                        // Documentation OpenAPI : les routes n'existent qu'en profil dev (springdoc désactivé ailleurs).
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()

                        // Les relances restent accessibles aux COMMERCIAUX.
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex

                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"code\":401,\"message\":\"Authentification requise\"}"
                            );
                        })

                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"code\":403,\"message\":\"Accès interdit\"}"
                            );
                        })
                );

        http.addFilterAfter(
                new CsrfCookieFilter(),
                CsrfFilter.class
        );

        return http.build();
    }
}
