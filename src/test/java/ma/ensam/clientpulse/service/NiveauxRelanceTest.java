package ma.ensam.clientpulse.service;

import ma.ensam.clientpulse.service.impl.NiveauxRelance;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NiveauxRelanceTest {

    @Test
    void aucunNiveauTantQueLEcheanceEstAPlusDeTroisJours() {
        assertThat(NiveauxRelance.niveauAtteint(-30)).isEqualTo(0);
        assertThat(NiveauxRelance.niveauAtteint(-4)).isEqualTo(0);
    }

    @Test
    void niveau1ATroisJoursAvantLEcheance() {
        assertThat(NiveauxRelance.niveauAtteint(-3)).isEqualTo(1);
        assertThat(NiveauxRelance.niveauAtteint(2)).isEqualTo(1);
    }

    @Test
    void lesNiveauxMontentAvecLeRetard() {
        assertThat(NiveauxRelance.niveauAtteint(3)).isEqualTo(2);
        assertThat(NiveauxRelance.niveauAtteint(14)).isEqualTo(2);
        assertThat(NiveauxRelance.niveauAtteint(15)).isEqualTo(3);
        assertThat(NiveauxRelance.niveauAtteint(29)).isEqualTo(3);
        assertThat(NiveauxRelance.niveauAtteint(30)).isEqualTo(4);
        assertThat(NiveauxRelance.niveauAtteint(44)).isEqualTo(4);
        assertThat(NiveauxRelance.niveauAtteint(45)).isEqualTo(5);
    }

    @Test
    void leNiveauMaximalResteLe5() {
        assertThat(NiveauxRelance.niveauAtteint(400)).isEqualTo(5);
    }

    @Test
    void libellesLisibles() {
        assertThat(NiveauxRelance.libelle(1)).isEqualTo("J-3");
        assertThat(NiveauxRelance.libelle(2)).isEqualTo("J+3");
        assertThat(NiveauxRelance.libelle(5)).isEqualTo("J+45");
    }
}
