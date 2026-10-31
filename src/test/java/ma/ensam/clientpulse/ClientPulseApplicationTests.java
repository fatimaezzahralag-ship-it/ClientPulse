package ma.ensam.clientpulse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ClientPulseApplicationTests {

    @Test
    void contextLoads() {
    }

}
