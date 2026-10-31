package ma.ensam.clientpulse;

import org.springframework.boot.SpringApplication;

public class TestClientPulseApplication {

    public static void main(String[] args) {
        SpringApplication.from(ClientPulseApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
