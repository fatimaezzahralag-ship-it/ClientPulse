package ma.ensam.clientpulse.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Active les méthodes @Scheduled (sans cette annotation, le job nocturne ne démarre jamais). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
