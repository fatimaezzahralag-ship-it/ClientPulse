package ma.ensam.clientpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClientPulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientPulseApplication.class, args);
    }

}
