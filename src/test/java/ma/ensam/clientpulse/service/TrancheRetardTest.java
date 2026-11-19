package ma.ensam.clientpulse.service;

import ma.ensam.clientpulse.domain.enums.TrancheRetard;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrancheRetardTest {

    @Test
    void pasEncoreEchuTantQueLeRetardEstZeroOuNegatif() {
        assertThat(TrancheRetard.pour(-10)).isEqualTo(TrancheRetard.NON_ECHU);
        assertThat(TrancheRetard.pour(0)).isEqualTo(TrancheRetard.NON_ECHU);
    }

    @Test
    void lesBornesDesTranches() {
        assertThat(TrancheRetard.pour(1)).isEqualTo(TrancheRetard.DE_1_A_30);
        assertThat(TrancheRetard.pour(30)).isEqualTo(TrancheRetard.DE_1_A_30);
        assertThat(TrancheRetard.pour(31)).isEqualTo(TrancheRetard.DE_31_A_60);
        assertThat(TrancheRetard.pour(60)).isEqualTo(TrancheRetard.DE_31_A_60);
        assertThat(TrancheRetard.pour(61)).isEqualTo(TrancheRetard.DE_61_A_90);
        assertThat(TrancheRetard.pour(90)).isEqualTo(TrancheRetard.DE_61_A_90);
        assertThat(TrancheRetard.pour(91)).isEqualTo(TrancheRetard.PLUS_DE_90);
    }
}
