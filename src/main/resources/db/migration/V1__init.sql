-- =============================================================================
-- ClientPulse - V1 : schéma initial
--
-- Source de vérité du schéma physique. Ce fichier a été construit à partir du
-- DDL généré par Hibernate 7 pour les entités du package domain.entity :
-- les types (ENUM natifs, BIT(1), DECIMAL, DATETIME(6)) sont donc exactement
-- ceux attendus par spring.jpa.hibernate.ddl-auto=validate.
--
-- Règles de travail :
--   * une migration Flyway déjà appliquée ne se modifie JAMAIS ;
--   * tout changement de schéma passe par un nouveau fichier V<n>__<sujet>.sql ;
--   * les noms de contraintes sont explicites (pas les hashes générés par Hibernate).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Sécurité : rôles, utilisateurs
-- -----------------------------------------------------------------------------

CREATE TABLE roles (
    id  BIGINT NOT NULL AUTO_INCREMENT,
    nom ENUM ('ADMIN', 'MANAGER', 'COMMERCIAL', 'COMPTABLE', 'ANALYSTE') NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_nom UNIQUE (nom)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE utilisateurs (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    nom           VARCHAR(100) NOT NULL,
    prenom        VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    mot_de_passe  VARCHAR(255) NOT NULL,
    actif         BIT(1)       NOT NULL DEFAULT b'1',
    PRIMARY KEY (id),
    CONSTRAINT uk_utilisateurs_email UNIQUE (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Table de jointure @ManyToMany Utilisateur <-> Role
CREATE TABLE utilisateurs_roles (
    utilisateur_id BIGINT NOT NULL,
    role_id        BIGINT NOT NULL,
    PRIMARY KEY (utilisateur_id, role_id),
    KEY idx_utilisateurs_roles_role (role_id),
    CONSTRAINT fk_utilisateurs_roles_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id),
    CONSTRAINT fk_utilisateurs_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- CRM : commerciaux, clients, interactions
-- -----------------------------------------------------------------------------

CREATE TABLE commerciaux (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    utilisateur_id   BIGINT NOT NULL,
    equipe           VARCHAR(255),
    objectif_mensuel DECIMAL(12, 2),
    PRIMARY KEY (id),
    CONSTRAINT uk_commerciaux_utilisateur UNIQUE (utilisateur_id),
    CONSTRAINT fk_commerciaux_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE clients (
    id                   BIGINT NOT NULL AUTO_INCREMENT,
    code                 VARCHAR(255) NOT NULL,
    raison_sociale       VARCHAR(255) NOT NULL,
    ice                  VARCHAR(20),
    email                VARCHAR(255),
    telephone            VARCHAR(255),
    adresse              VARCHAR(255),
    ville                VARCHAR(255),
    secteur              VARCHAR(255),
    delai_paiement_jours INT,
    type                 ENUM ('ENTREPRISE', 'PARTICULIER') NOT NULL,
    statut               ENUM ('PROSPECT', 'ACTIF', 'INACTIF') NOT NULL,
    commercial_id        BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_clients_code UNIQUE (code),
    KEY idx_clients_commercial (commercial_id),
    KEY idx_clients_statut_type (statut, type),
    KEY idx_clients_ville (ville),
    CONSTRAINT fk_clients_commercial
        FOREIGN KEY (commercial_id) REFERENCES commerciaux (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE interactions (
    id        BIGINT NOT NULL AUTO_INCREMENT,
    client_id BIGINT NOT NULL,
    `date`    DATETIME(6) NOT NULL,
    resume    VARCHAR(2000),
    statut    ENUM ('OUVERTE', 'EN_COURS', 'CLOTUREE'),
    type      ENUM ('APPEL', 'EMAIL', 'REUNION', 'RECLAMATION') NOT NULL,
    PRIMARY KEY (id),
    KEY idx_interactions_client (client_id),
    KEY idx_interactions_date (`date`),
    CONSTRAINT fk_interactions_client
        FOREIGN KEY (client_id) REFERENCES clients (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Numérotation continue : une ligne par (type de document, année)
-- -----------------------------------------------------------------------------

CREATE TABLE sequence_numerotation (
    id             BIGINT NOT NULL AUTO_INCREMENT,
    type           ENUM ('DEVIS', 'FACTURE', 'AVOIR') NOT NULL,
    annee          INT NOT NULL,
    dernier_numero INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_sequence_numerotation_type_annee UNIQUE (type, annee)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Devis
-- -----------------------------------------------------------------------------

CREATE TABLE devis (
    id                BIGINT NOT NULL AUTO_INCREMENT,
    numero            VARCHAR(30) NOT NULL,
    date_emission     DATE NOT NULL,
    date_validite     DATE,
    statut            ENUM ('BROUILLON', 'ENVOYE', 'ACCEPTE', 'REFUSE', 'EXPIRE') NOT NULL,
    total_ht          DECIMAL(15, 2) NOT NULL,
    total_tva         DECIMAL(15, 2) NOT NULL,
    total_ttc         DECIMAL(15, 2) NOT NULL,
    client_id         BIGINT NOT NULL,
    commercial_id     BIGINT,
    date_creation     DATETIME(6),
    date_modification DATETIME(6),
    cree_par          VARCHAR(100),
    modifie_par       VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT uk_devis_numero UNIQUE (numero),
    KEY idx_devis_client (client_id),
    KEY idx_devis_commercial (commercial_id),
    CONSTRAINT fk_devis_client
        FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_devis_commercial
        FOREIGN KEY (commercial_id) REFERENCES commerciaux (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE ligne_devis (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    devis_id        BIGINT NOT NULL,
    designation     VARCHAR(255) NOT NULL,
    quantite        DECIMAL(15, 2) NOT NULL,
    prix_unitaire_ht DECIMAL(15, 2) NOT NULL,
    remise          DECIMAL(5, 2),
    taux_tva        DECIMAL(5, 2) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_ligne_devis_devis (devis_id),
    CONSTRAINT fk_ligne_devis_devis
        FOREIGN KEY (devis_id) REFERENCES devis (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Factures, avoirs, lignes
-- -----------------------------------------------------------------------------

CREATE TABLE facture (
    id                 BIGINT NOT NULL AUTO_INCREMENT,
    numero             VARCHAR(30),
    type               ENUM ('FACTURE', 'AVOIR') NOT NULL,
    date_emission      DATE,
    date_echeance      DATE,
    statut             ENUM ('BROUILLON', 'EMISE', 'PARTIELLEMENT_PAYEE', 'PAYEE', 'EN_RETARD', 'ANNULEE') NOT NULL,
    total_ht           DECIMAL(15, 2) NOT NULL,
    total_tva          DECIMAL(15, 2) NOT NULL,
    total_ttc          DECIMAL(15, 2) NOT NULL,
    client_id          BIGINT NOT NULL,
    devis_id           BIGINT,
    facture_origine_id BIGINT,
    date_creation      DATETIME(6),
    date_modification  DATETIME(6),
    cree_par           VARCHAR(100),
    modifie_par        VARCHAR(100),
    PRIMARY KEY (id),
    CONSTRAINT uk_facture_numero UNIQUE (numero),
    CONSTRAINT uk_facture_devis UNIQUE (devis_id),
    KEY idx_facture_client (client_id),
    KEY idx_facture_origine (facture_origine_id),
    KEY idx_facture_statut_echeance (statut, date_echeance),
    CONSTRAINT fk_facture_client
        FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_facture_devis
        FOREIGN KEY (devis_id) REFERENCES devis (id),
    CONSTRAINT fk_facture_origine
        FOREIGN KEY (facture_origine_id) REFERENCES facture (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE ligne_facture (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    facture_id       BIGINT NOT NULL,
    designation      VARCHAR(255) NOT NULL,
    quantite         DECIMAL(15, 2) NOT NULL,
    prix_unitaire_ht DECIMAL(15, 2) NOT NULL,
    remise           DECIMAL(5, 2),
    taux_tva         DECIMAL(5, 2) NOT NULL,
    montant_ht       DECIMAL(15, 2),
    montant_tva      DECIMAL(15, 2),
    PRIMARY KEY (id),
    KEY idx_ligne_facture_facture (facture_id),
    CONSTRAINT fk_ligne_facture_facture
        FOREIGN KEY (facture_id) REFERENCES facture (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Paiements
-- -----------------------------------------------------------------------------

CREATE TABLE paiement (
    id                BIGINT NOT NULL AUTO_INCREMENT,
    facture_id        BIGINT NOT NULL,
    date_paiement     DATE NOT NULL,
    montant           DECIMAL(15, 2) NOT NULL,
    mode_paiement     ENUM ('VIREMENT', 'CHEQUE', 'ESPECES', 'CARTE', 'EFFET') NOT NULL,
    reference         VARCHAR(100),
    date_creation     DATETIME(6),
    date_modification DATETIME(6),
    cree_par          VARCHAR(100),
    modifie_par       VARCHAR(100),
    PRIMARY KEY (id),
    KEY idx_paiement_facture (facture_id),
    KEY idx_paiement_date (date_paiement),
    CONSTRAINT fk_paiement_facture
        FOREIGN KEY (facture_id) REFERENCES facture (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
