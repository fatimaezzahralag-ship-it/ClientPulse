-- =============================================================================
-- ClientPulse - V5 : segmentation RFM, relances commerciales et automatisation
--
-- Personne 2 - J4.
--
-- V3 est volontairement libre (numéro réservé par une autre branche), V4 est
-- déjà utilisée par le scoring de J3.
--
-- Deux ajouts :
--   1. les seuils RFM, stockés dans parametres_scoring comme tous les autres
--      paramètres du moteur (aucun seuil codé en dur dans le Java) ;
--   2. la table des relances. P1 n'ayant finalement pas livré cette entité, ce
--      script fournit le contrat commun utilisé par P1 (PAIEMENT) et P2
--      (COMMERCIALE). Aucune seconde entité Relance n'est créée ailleurs.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Seuils RFM (valeurs par défaut, modifiables via /api/parametres par un ADMIN)
--
-- R = nombre de jours depuis le dernier achat (facture émise non annulée).
-- F = nombre d'achats sur les 12 derniers mois.
-- M = montant TTC des achats sur les 12 derniers mois.
--
-- Règles de classement retenues (évaluées dans cet ordre) :
--   1. aucun achat + activité ancienne            -> PERDU
--   2. aucun achat + activité récente              -> NOUVEAU
--   3. dernier achat >= RFM_PERDU_JOURS            -> PERDU
--   4. dernier achat >= RFM_HIBERNATION_JOURS      -> HIBERNATION
--   5. premier achat <= RFM_NOUVEAU_JOURS          -> NOUVEAU
--   6. R <= CHAMPION et F >= CHAMPION et M >= CHAMPION -> CHAMPION
--   7. R <= LOYAL et F >= LOYAL et M >= LOYAL      -> LOYAL
--   8. sinon                                       -> A_RISQUE
--
-- Le seuil d'hibernation est fixé à 240 jours (8 mois) pour qu'un client vu
-- il y a 180 jours avec peu d'achats reste classé A_RISQUE, comme dans
-- l'exemple métier du planning : la relance commerciale doit encore agir.
-- -----------------------------------------------------------------------------
INSERT INTO parametres_scoring (nom, valeur) VALUES
    ('RFM_RECENCE_CHAMPION',     30.0000),
    ('RFM_RECENCE_LOYAL',        90.0000),
    ('RFM_FREQUENCE_CHAMPION',   10.0000),
    ('RFM_FREQUENCE_LOYAL',       4.0000),
    ('RFM_MONETAIRE_CHAMPION', 50000.0000),
    ('RFM_MONETAIRE_LOYAL',    10000.0000),
    ('RFM_NOUVEAU_JOURS',        90.0000),
    ('RFM_HIBERNATION_JOURS',   240.0000),
    ('RFM_PERDU_JOURS',         365.0000);

-- -----------------------------------------------------------------------------
-- Relances : nature PAIEMENT (P1) et COMMERCIALE (P2) partagent la même table.
-- L'anti-doublon est appliqué par le service : une seule relance active
-- (A_FAIRE ou EN_COURS) par client et par nature. L'index ci-dessous soutient
-- cette recherche sans imposer de contrainte sur les relances terminées.
-- -----------------------------------------------------------------------------
CREATE TABLE relances (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    client_id         BIGINT       NOT NULL,
    commercial_id     BIGINT       NULL,
    nature            ENUM ('PAIEMENT', 'COMMERCIALE') NOT NULL,
    statut            ENUM ('A_FAIRE', 'EN_COURS', 'TERMINEE') NOT NULL,
    motif             TEXT         NULL,
    resultat          ENUM ('INTERESSE', 'PAS_INTERESSE', 'RENDEZ_VOUS_PRIS',
                            'INJOIGNABLE', 'SANS_REPONSE', 'AUTRE') NULL,
    commentaire       TEXT         NULL,
    date_echeance     DATE         NULL,
    date_creation     DATETIME(6)  NOT NULL,
    date_modification DATETIME(6)  NULL,
    cree_par          VARCHAR(100) NULL,
    modifie_par       VARCHAR(100) NULL,
    PRIMARY KEY (id),
    KEY idx_relances_client_nature_statut (client_id, nature, statut),
    KEY idx_relances_commercial_statut (commercial_id, statut),
    CONSTRAINT fk_relances_client
        FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_relances_commercial
        FOREIGN KEY (commercial_id) REFERENCES commerciaux (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
