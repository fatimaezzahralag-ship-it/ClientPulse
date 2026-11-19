-- =============================================================================
-- ClientPulse - V4 : moteur de scoring (scores clients + paramètres)
--
-- Personne 2 - J3.
--
-- Cette migration crée les deux tables du scoring et insère les paramètres
-- métier par défaut. Aucun poids ni seuil n'est codé en dur dans le Java : le
-- calcul lit systématiquement ces valeurs en base, ce qui permet à
-- l'entreprise de les ajuster sans recompiler le backend.
--
-- Les valeurs ci-dessous sont des valeurs métier réalistes pour un CRM B2B
-- marocain ; elles restent modifiables par un simple UPDATE.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Paramètres du moteur (poids, seuils de niveau, seuils internes des facteurs)
-- -----------------------------------------------------------------------------
CREATE TABLE parametres_scoring (
    id     BIGINT         NOT NULL AUTO_INCREMENT,
    nom    VARCHAR(100)   NOT NULL,
    valeur DECIMAL(15, 4) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_parametres_scoring_nom UNIQUE (nom)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Historique des scores calculés
-- -----------------------------------------------------------------------------
CREATE TABLE scores_client (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    client_id           BIGINT       NOT NULL,
    date_calcul         DATE         NOT NULL,
    score_risque        DECIMAL(5,2) NOT NULL,
    niveau              ENUM ('FAIBLE', 'MOYEN', 'ELEVE', 'CRITIQUE') NOT NULL,
    segment             ENUM ('CHAMPION', 'LOYAL', 'NOUVEAU', 'A_RISQUE', 'HIBERNATION', 'PERDU') NOT NULL,
    facteurs_explicatifs TEXT,
    PRIMARY KEY (id),
    KEY idx_scores_client_client_date (client_id, date_calcul),
    CONSTRAINT fk_scores_client_client
        FOREIGN KEY (client_id) REFERENCES clients (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Paramètres par défaut
--
-- Les quatre poids somment à 1 : le score final reste donc dans l'intervalle
-- 0-100, ce qui rend les seuils lisibles pour les utilisateurs.
-- -----------------------------------------------------------------------------
INSERT INTO parametres_scoring (nom, valeur) VALUES
    -- Poids des facteurs
    ('POIDS_INACTIVITE',       0.3000),
    ('POIDS_BAISSE_ACHATS',    0.2500),
    ('POIDS_RECLAMATIONS',     0.1500),
    ('POIDS_RETARD_PAIEMENT',  0.3000),

    -- Seuils de niveau de risque (score global sur 100)
    ('SEUIL_RISQUE_MOYEN',     40.0000),
    ('SEUIL_RISQUE_ELEVE',     70.0000),
    ('SEUIL_RISQUE_CRITIQUE',  90.0000),

    -- Inactivité : nombre de jours depuis la dernière interaction
    ('SEUIL_INACTIVITE_FAIBLE',  30.0000),
    ('SEUIL_INACTIVITE_MOYEN',   90.0000),
    ('SEUIL_INACTIVITE_ELEVE',  180.0000),

    -- Baisse des achats : pourcentage de diminution sur 3 mois
    ('SEUIL_BAISSE_FAIBLE',  20.0000),
    ('SEUIL_BAISSE_MOYEN',   50.0000),
    ('SEUIL_BAISSE_ELEVE',   75.0000),

    -- Réclamations ouvertes
    ('SEUIL_RECLAMATIONS_MOYEN',  1.0000),
    ('SEUIL_RECLAMATIONS_ELEVE',  2.0000),

    -- Retard moyen de paiement en jours
    ('SEUIL_RETARD_FAIBLE',   7.0000),
    ('SEUIL_RETARD_MOYEN',   30.0000);
