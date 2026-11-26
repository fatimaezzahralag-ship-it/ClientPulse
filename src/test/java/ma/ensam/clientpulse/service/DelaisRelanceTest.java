package ma.ensam.clientpulse.service;

import ma.ensam.clientpulse.service.impl.DelaisRelance;
import ma.ensam.clientpulse.service.impl.NiveauxRelance;
import ma.ensam.clientpulse.service.scoring.NomsParametres;
import ma.ensam.clientpulse.service.scoring.ParametreScoringDaoStub;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DelaisRelanceTest {

    @Test
    void sansParametreEnBaseOnRetrouveLesDelaisHistoriques() {
        int[] decalages = new DelaisRelance(new ParametreScoringDaoStub()).decalages();

        assertThat(decalages).containsExactly(-3, 3, 15, 30, 45);
        assertThat(DelaisRelance.parDefaut().decalages()).containsExactly(-3, 3, 15, 30, 45);
    }

    @Test
    void lesParametresDeLaBaseRemplacentLesValeursParDefaut() {
        ParametreScoringDaoStub parametres = new ParametreScoringDaoStub()
                .avec(NomsParametres.DELAI_RELANCE_N1, "7")      // 7 jours AVANT l'échéance
                .avec(NomsParametres.DELAI_RELANCE_N2, "5")
                .avec(NomsParametres.DELAI_RELANCE_N5, "60");

        int[] decalages = new DelaisRelance(parametres).decalages();

        assertThat(decalages).containsExactly(-7, 5, 15, 30, 60);
    }

    @Test
    void leNiveauAtteintSuitLesDelaisParametres() {
        int[] decalages = {-7, 5, 15, 30, 60};

        assertThat(NiveauxRelance.niveauAtteint(-8, decalages)).isEqualTo(0);
        assertThat(NiveauxRelance.niveauAtteint(-7, decalages)).isEqualTo(1);
        assertThat(NiveauxRelance.niveauAtteint(5, decalages)).isEqualTo(2);
        assertThat(NiveauxRelance.niveauAtteint(45, decalages)).isEqualTo(4);   // 45 n'est plus un seuil
        assertThat(NiveauxRelance.niveauAtteint(60, decalages)).isEqualTo(5);
        assertThat(NiveauxRelance.libelle(1, decalages)).isEqualTo("J-7");
        assertThat(NiveauxRelance.libelle(5, decalages)).isEqualTo("J+60");
    }

    @Test
    void lesAnciennesMethodesGardentLeurComportement() {
        assertThat(NiveauxRelance.niveauAtteint(45)).isEqualTo(5);
        assertThat(NiveauxRelance.libelle(1)).isEqualTo("J-3");
        assertThat(NiveauxRelance.decalagesParDefaut()).containsExactly(-3, 3, 15, 30, 45);
    }
}
