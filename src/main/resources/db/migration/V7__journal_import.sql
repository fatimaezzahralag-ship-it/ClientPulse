-- =============================================================================
-- ClientPulse - V7 : journal des imports CSV
--
-- Personne 3 - J4. Trace chaque import : fichier, lignes lues, importées, rejetées,
-- et l'auteur (colonnes d'audit habituelles, remplies par AuditConfig).
-- Migration ADDITIVE : aucune table existante n'est modifiée.
-- =============================================================================

CREATE TABLE journal_import (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    fichier           VARCHAR(255) NOT NULL,
    lignes_lues       INT          NOT NULL,
    lignes_importees  INT          NOT NULL,
    lignes_rejetees   INT          NOT NULL,
    date_creation     DATETIME(6)  NULL,
    date_modification DATETIME(6)  NULL,
    cree_par          VARCHAR(100) NULL,
    modifie_par       VARCHAR(100) NULL,
    PRIMARY KEY (id),
    KEY idx_journal_import_date (date_creation)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
