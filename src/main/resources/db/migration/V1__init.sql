-- =====================================================================================
-- V1 : schéma initial de ClientPulse (19 tables, MySQL 8, InnoDB, utf8mb4)
-- Source : Livrable 2, chapitre 6 (modèle relationnel). Les noms de colonnes suivent le
-- diagramme de classes : tout ce qui est écrit ici doit correspondre aux entités JPA.
-- Les migrations déjà appliquées ne se modifient JAMAIS : un changement = une nouvelle version (V5, V6...).
-- =====================================================================================

-- ---------------------------------------------------------------- Sécurité
CREATE TABLE utilisateur (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    nom          VARCHAR(100) NOT NULL,
    prenom       VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL,
    mot_de_passe VARCHAR(100) NOT NULL,              -- haché (BCrypt), jamais en clair
    actif        BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_utilisateur_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE role (
    id  BIGINT      NOT NULL AUTO_INCREMENT,
    nom VARCHAR(50) NOT NULL,                        -- valeurs de RoleNom : insérées par V2 (P2)
    PRIMARY KEY (id),
    CONSTRAINT uk_role_nom UNIQUE (nom)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE utilisateur_role (
    utilisateur_id BIGINT NOT NULL,
    role_id        BIGINT NOT NULL,
    PRIMARY KEY (utilisateur_id, role_id),
    CONSTRAINT fk_ur_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role        FOREIGN KEY (role_id)        REFERENCES role (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE commercial (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    utilisateur_id   BIGINT        NOT NULL,
    equipe           VARCHAR(100),
    objectif_mensuel DECIMAL(15,2),
    PRIMARY KEY (id),
    CONSTRAINT uk_commercial_utilisateur UNIQUE (utilisateur_id),   -- 1 utilisateur = 0 ou 1 commercial
    CONSTRAINT fk_commercial_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------- CRM
-- Les colonnes type / statut ne portent pas de CHECK ici : les valeurs de TypeClient, StatutClient,
-- TypeInteraction et StatutInteraction sont fixées par P2. P2 ajoutera les CHECK dans une migration.
CREATE TABLE client (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    code                  VARCHAR(30)  NOT NULL,
    raison_sociale        VARCHAR(200) NOT NULL,
    ice                   VARCHAR(30),
    type                  VARCHAR(30),
    email                 VARCHAR(150),
    telephone             VARCHAR(30),
    adresse               VARCHAR(255),
    ville                 VARCHAR(100),
    secteur               VARCHAR(100),
    delai_paiement_jours  INT          NOT NULL DEFAULT 30,
    statut                VARCHAR(30)  NOT NULL,
    date_creation         DATETIME,
    commercial_id         BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_client_code UNIQUE (code),
    CONSTRAINT fk_client_commercial FOREIGN KEY (commercial_id) REFERENCES commercial (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE interaction (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    client_id        BIGINT       NOT NULL,
    type             VARCHAR(30)  NOT NULL,
    date_interaction DATETIME     NOT NULL,
    resume           VARCHAR(1000),
    statut           VARCHAR(30)  NOT NULL,
    date_creation     DATETIME,
    date_modification DATETIME,
    cree_par          VARCHAR(100),
    modifie_par       VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_interaction_client FOREIGN KEY (client_id) REFERENCES client (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE contrat (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    client_id      BIGINT        NOT NULL,
    reference      VARCHAR(50)   NOT NULL,
    date_debut     DATE          NOT NULL,
    date_fin       DATE,
    montant_annuel DECIMAL(15,2),
    renouvelable   BOOLEAN       NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT fk_contrat_client FOREIGN KEY (client_id) REFERENCES client (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------- Catalogue
CREATE TABLE taux_tva (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    libelle VARCHAR(50)  NOT NULL,
    taux    DECIMAL(5,2) NOT NULL,                   -- en pourcentage : 20.00 = 20 %
    actif   BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE produit (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    reference        VARCHAR(50)   NOT NULL,
    designation      VARCHAR(255)  NOT NULL,
    prix_unitaire_ht DECIMAL(15,2) NOT NULL,
    marge            DECIMAL(5,2),
    actif            BOOLEAN       NOT NULL DEFAULT TRUE,
    taux_tva_id      BIGINT        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_produit_reference UNIQUE (reference),
    CONSTRAINT fk_produit_taux_tva FOREIGN KEY (taux_tva_id) REFERENCES taux_tva (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------- Facturation
CREATE TABLE devis (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    numero            VARCHAR(30)   NOT NULL,
    date_emission     DATE          NOT NULL,
    date_validite     DATE,
    statut            VARCHAR(20)   NOT NULL,
    total_ht          DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_tva         DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_ttc         DECIMAL(15,2) NOT NULL DEFAULT 0,
    client_id         BIGINT        NOT NULL,
    commercial_id     BIGINT,
    date_creation     DATETIME,
    date_modification DATETIME,
    cree_par          VARCHAR(100),
    modifie_par       VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT uk_devis_numero UNIQUE (numero),
    CONSTRAINT ck_devis_statut CHECK (statut IN ('BROUILLON','ENVOYE','ACCEPTE','REFUSE','EXPIRE')),
    CONSTRAINT fk_devis_client     FOREIGN KEY (client_id)     REFERENCES client (id),
    CONSTRAINT fk_devis_commercial FOREIGN KEY (commercial_id) REFERENCES commercial (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE ligne_devis (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    devis_id         BIGINT        NOT NULL,
    produit_id       BIGINT,
    designation      VARCHAR(255)  NOT NULL,
    quantite         DECIMAL(15,2) NOT NULL,
    prix_unitaire_ht DECIMAL(15,2) NOT NULL,
    remise           DECIMAL(5,2)  NOT NULL DEFAULT 0,
    taux_tva         DECIMAL(5,2)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ligne_devis_devis   FOREIGN KEY (devis_id)   REFERENCES devis (id) ON DELETE CASCADE,
    CONSTRAINT fk_ligne_devis_produit FOREIGN KEY (produit_id) REFERENCES produit (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE facture (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    numero             VARCHAR(30),                  -- NULL tant que la facture est en brouillon
    type               VARCHAR(20)   NOT NULL,
    date_emission      DATE,
    date_echeance      DATE,
    statut             VARCHAR(30)   NOT NULL,
    total_ht           DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_tva          DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_ttc          DECIMAL(15,2) NOT NULL DEFAULT 0,
    client_id          BIGINT        NOT NULL,
    devis_id           BIGINT,
    facture_origine_id BIGINT,                       -- pour un avoir : la facture corrigée
    date_creation      DATETIME,
    date_modification  DATETIME,
    cree_par           VARCHAR(100),
    modifie_par        VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT uk_facture_numero UNIQUE (numero),    -- plusieurs NULL autorisés en MySQL
    CONSTRAINT uk_facture_devis  UNIQUE (devis_id),  -- une facture au plus par devis
    CONSTRAINT ck_facture_type   CHECK (type IN ('FACTURE','AVOIR')),
    CONSTRAINT ck_facture_statut CHECK (statut IN
        ('BROUILLON','EMISE','PARTIELLEMENT_PAYEE','PAYEE','EN_RETARD','ANNULEE')),
    CONSTRAINT fk_facture_client  FOREIGN KEY (client_id)          REFERENCES client (id),
    CONSTRAINT fk_facture_devis   FOREIGN KEY (devis_id)           REFERENCES devis (id),
    CONSTRAINT fk_facture_origine FOREIGN KEY (facture_origine_id) REFERENCES facture (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE ligne_facture (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    facture_id       BIGINT        NOT NULL,
    produit_id       BIGINT,
    designation      VARCHAR(255)  NOT NULL,
    quantite         DECIMAL(15,2) NOT NULL,
    prix_unitaire_ht DECIMAL(15,2) NOT NULL,
    remise           DECIMAL(5,2)  NOT NULL DEFAULT 0,
    taux_tva         DECIMAL(5,2)  NOT NULL,         -- copié à l'émission : ne change plus
    montant_ht       DECIMAL(15,2),
    montant_tva      DECIMAL(15,2),
    PRIMARY KEY (id),
    CONSTRAINT fk_ligne_facture_facture FOREIGN KEY (facture_id) REFERENCES facture (id) ON DELETE CASCADE,
    CONSTRAINT fk_ligne_facture_produit FOREIGN KEY (produit_id) REFERENCES produit (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Le reste à payer n'est PAS stocké : il se calcule (total TTC - somme des paiements).
CREATE TABLE paiement (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    facture_id        BIGINT        NOT NULL,
    date_paiement     DATE          NOT NULL,
    montant           DECIMAL(15,2) NOT NULL,
    mode_paiement     VARCHAR(20)   NOT NULL,
    reference         VARCHAR(100),
    date_creation     DATETIME,
    date_modification DATETIME,
    cree_par          VARCHAR(100),
    modifie_par       VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT ck_paiement_montant CHECK (montant > 0),
    CONSTRAINT ck_paiement_mode CHECK (mode_paiement IN ('VIREMENT','CHEQUE','ESPECES','CARTE','EFFET')),
    CONSTRAINT fk_paiement_facture FOREIGN KEY (facture_id) REFERENCES facture (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE sequence_numerotation (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    type           VARCHAR(20) NOT NULL,
    annee          INT         NOT NULL,
    dernier_numero INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_sequence_type_annee UNIQUE (type, annee),
    CONSTRAINT ck_sequence_type CHECK (type IN ('DEVIS','FACTURE','AVOIR'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------- Relances et analytique
CREATE TABLE relance (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    nature               VARCHAR(20)  NOT NULL,
    niveau               INT          NOT NULL DEFAULT 1,
    date_creation        DATETIME     NOT NULL,
    echeance             DATE,
    statut               VARCHAR(20)  NOT NULL,
    resultat             VARCHAR(255),
    contenu              VARCHAR(1000),
    suggeree_par_systeme BOOLEAN      NOT NULL DEFAULT FALSE,
    client_id            BIGINT       NOT NULL,
    facture_id           BIGINT,                     -- renseignée seulement pour une relance de PAIEMENT
    affecte_a_id         BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT ck_relance_nature CHECK (nature IN ('PAIEMENT','COMMERCIALE')),
    CONSTRAINT ck_relance_statut CHECK (statut IN ('A_FAIRE','EN_COURS','TERMINEE')),
    -- Garde-fou : une seule relance par facture, par nature et par niveau (les NULL ne se comparent pas)
    CONSTRAINT uk_relance_facture_nature_niveau UNIQUE (facture_id, nature, niveau),
    CONSTRAINT fk_relance_client     FOREIGN KEY (client_id)    REFERENCES client (id),
    CONSTRAINT fk_relance_facture    FOREIGN KEY (facture_id)   REFERENCES facture (id),
    CONSTRAINT fk_relance_affecte_a  FOREIGN KEY (affecte_a_id) REFERENCES utilisateur (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE score_client (
    id                       BIGINT      NOT NULL AUTO_INCREMENT,
    client_id                BIGINT      NOT NULL,
    date_calcul              DATETIME    NOT NULL,
    score_risque             INT         NOT NULL,
    niveau                   VARCHAR(30),
    note_r                   INT,
    note_f                   INT,
    note_m                   INT,
    segment                  VARCHAR(30),
    score_fiabilite_paiement INT,
    facteurs_explicatifs     TEXT,
    PRIMARY KEY (id),
    CONSTRAINT ck_score_risque CHECK (score_risque BETWEEN 0 AND 100),
    CONSTRAINT fk_score_client FOREIGN KEY (client_id) REFERENCES client (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE parametre_scoring (
    id     BIGINT        NOT NULL AUTO_INCREMENT,
    nom    VARCHAR(100)  NOT NULL,
    valeur DECIMAL(15,4) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_parametre_nom UNIQUE (nom)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE journal_import (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    fichier           VARCHAR(255) NOT NULL,
    lignes_lues       INT          NOT NULL DEFAULT 0,
    lignes_rejetees   INT          NOT NULL DEFAULT 0,
    date_creation     DATETIME,
    date_modification DATETIME,
    cree_par          VARCHAR(100),
    modifie_par       VARCHAR(100),
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
