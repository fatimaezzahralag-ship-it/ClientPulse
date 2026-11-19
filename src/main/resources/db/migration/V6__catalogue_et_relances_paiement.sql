-- =============================================================================
-- ClientPulse - V6 : catalogue (taux de TVA, produits) et relances de paiement
--
-- Personne 1 - J3/J4.
--
-- V3 reste libre : une migration insérée avant V5 échouerait sur les bases où V5 est déjà appliquée.
-- Tout ce qui est ajouté ici est ADDITIF : aucune colonne existante n'est modifiée ni supprimée.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Taux de TVA (paramétrables ensuite via /api/taux-tva)
-- -----------------------------------------------------------------------------
CREATE TABLE taux_tva (
    id      BIGINT        NOT NULL AUTO_INCREMENT,
    libelle VARCHAR(50)   NOT NULL,
    taux    DECIMAL(5, 2) NOT NULL,
    actif   BOOLEAN       NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO taux_tva (libelle, taux, actif) VALUES
    ('TVA 20 %',      20.00, TRUE),
    ('TVA 14 %',      14.00, TRUE),
    ('TVA 10 %',      10.00, TRUE),
    ('TVA 7 %',        7.00, TRUE),
    ('Exonéré (0 %)',  0.00, TRUE);

-- -----------------------------------------------------------------------------
-- Produits du catalogue
-- -----------------------------------------------------------------------------
CREATE TABLE produit (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    reference        VARCHAR(50)    NOT NULL,
    designation      VARCHAR(255)   NOT NULL,
    prix_unitaire_ht DECIMAL(15, 2) NOT NULL,
    marge            DECIMAL(5, 2),
    actif            BOOLEAN        NOT NULL DEFAULT TRUE,
    taux_tva_id      BIGINT         NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_produit_reference UNIQUE (reference),
    CONSTRAINT fk_produit_taux_tva
        FOREIGN KEY (taux_tva_id) REFERENCES taux_tva (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Une ligne de devis ou de facture peut venir d'un produit du catalogue (facultatif)
-- -----------------------------------------------------------------------------
ALTER TABLE ligne_devis
    ADD COLUMN produit_id BIGINT NULL,
    ADD CONSTRAINT fk_ligne_devis_produit
        FOREIGN KEY (produit_id) REFERENCES produit (id);

ALTER TABLE ligne_facture
    ADD COLUMN produit_id BIGINT NULL,
    ADD CONSTRAINT fk_ligne_facture_produit
        FOREIGN KEY (produit_id) REFERENCES produit (id);

-- -----------------------------------------------------------------------------
-- Relances de paiement : facture concernée et niveau (1 = J-3, 2 = J+3, 3 = J+15, 4 = J+30, 5 = J+45).
-- Les deux colonnes restent vides pour une relance COMMERCIALE (P2).
-- La contrainte unique garantit une seule relance par facture, nature et niveau, même si le job tourne
-- deux fois (MySQL ignore les lignes où facture_id est NULL).
-- -----------------------------------------------------------------------------
ALTER TABLE relances
    ADD COLUMN facture_id BIGINT NULL,
    ADD COLUMN niveau     INT    NULL,
    ADD CONSTRAINT fk_relances_facture
        FOREIGN KEY (facture_id) REFERENCES facture (id),
    ADD CONSTRAINT uk_relances_facture_nature_niveau
        UNIQUE (facture_id, nature, niveau);
