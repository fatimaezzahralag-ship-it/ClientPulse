package ma.ensam.clientpulse.service.interfaces;

import ma.ensam.clientpulse.dto.DashboardDto;

import java.time.LocalDate;

/**
 * Tableau de bord financier et commercial (exigences C4, C5, C6 ; cahier des charges 7.10, nommé KpiService).
 */
public interface DashboardService {

    /** Indicateurs au jour même, sur les {@code periodeMois} derniers mois (1 à 12). */
    DashboardDto calculer(int periodeMois);

    /** Idem à une date de référence donnée (tests, simulation « passage du temps » de la démo). */
    DashboardDto calculer(int periodeMois, LocalDate jour);

    /** Export CSV (séparateur « ; », encodage UTF-8 avec BOM pour Excel) des indicateurs et de la série mensuelle. */
    String exporterCsv(int periodeMois, LocalDate jour);
}
