-- =============================================================================
-- ClientPulse - vérifications de la partie Personne 3 (J1-J2)
--
-- À exécuter dans phpMyAdmin (onglet SQL) ou en ligne de commande :
--   mysql -u root clientpulse < docs/sql/verifications-p3.sql
--
-- Chaque bloc répond à une question précise sur le socle de persistance.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Flyway a-t-il réellement exécuté les migrations ?
--    Attendu : deux lignes, V1 (init) et V2 (roles), success = 1.
-- -----------------------------------------------------------------------------
SELECT installed_rank, version, description, script, success, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;

-- -----------------------------------------------------------------------------
-- 2. Les 12 tables du modèle existent-elles ?
--    Attendu : 12 lignes "OK" (flyway_schema_history est une table technique,
--    elle n'est pas dans la liste).
-- -----------------------------------------------------------------------------
SELECT a.table_attendue,
       CASE WHEN t.table_name IS NULL THEN 'MANQUANTE' ELSE 'OK' END AS etat,
       t.engine,
       t.table_collation
FROM (
    SELECT 'roles' AS table_attendue
    UNION ALL SELECT 'utilisateurs'
    UNION ALL SELECT 'utilisateurs_roles'
    UNION ALL SELECT 'commerciaux'
    UNION ALL SELECT 'clients'
    UNION ALL SELECT 'interactions'
    UNION ALL SELECT 'sequence_numerotation'
    UNION ALL SELECT 'devis'
    UNION ALL SELECT 'ligne_devis'
    UNION ALL SELECT 'facture'
    UNION ALL SELECT 'ligne_facture'
    UNION ALL SELECT 'paiement'
) a
LEFT JOIN information_schema.tables t
       ON t.table_schema = DATABASE()
      AND t.table_name = a.table_attendue
ORDER BY a.table_attendue;

-- -----------------------------------------------------------------------------
-- 3. Les clés primaires, clés étrangères et contraintes d'unicité sont-elles là ?
--    Attendu : des noms explicites uk_*, fk_* (et non les hashes d'Hibernate).
-- -----------------------------------------------------------------------------
SELECT table_name, constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_schema = DATABASE()
ORDER BY constraint_type, table_name, constraint_name;

-- -----------------------------------------------------------------------------
-- 4. Les index utiles existent-ils (FK + colonnes de filtre) ?
-- -----------------------------------------------------------------------------
SELECT table_name, index_name,
       GROUP_CONCAT(column_name ORDER BY seq_in_index) AS colonnes,
       IF(non_unique = 0, 'UNIQUE', '') AS type_index
FROM information_schema.statistics
WHERE table_schema = DATABASE()
GROUP BY table_name, index_name, non_unique
ORDER BY table_name, index_name;

-- -----------------------------------------------------------------------------
-- 5. La colonne clients.ice (et non identifiant_fiscal) est-elle correcte ?
-- -----------------------------------------------------------------------------
SELECT column_name, column_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'clients'
ORDER BY ordinal_position;

-- -----------------------------------------------------------------------------
-- 6. Les 5 rôles viennent-ils de la migration V2 ?
-- -----------------------------------------------------------------------------
SELECT id, nom FROM roles ORDER BY id;

-- -----------------------------------------------------------------------------
-- 7. Les comptes de démonstration sont-ils créés avec leurs rôles ?
--    Le mot de passe doit être un hash BCrypt ($2a$...), jamais en clair.
-- -----------------------------------------------------------------------------
SELECT u.id,
       u.email,
       CAST(u.actif AS UNSIGNED) AS actif,
       LEFT(u.mot_de_passe, 7) AS debut_hash,
       GROUP_CONCAT(r.nom ORDER BY r.nom) AS roles
FROM utilisateurs u
LEFT JOIN utilisateurs_roles ur ON ur.utilisateur_id = u.id
LEFT JOIN roles r ON r.id = ur.role_id
GROUP BY u.id, u.email, u.actif, u.mot_de_passe
ORDER BY u.id;

-- -----------------------------------------------------------------------------
-- 8. Le schéma est-il vide de données métier (base fraîche) ?
-- -----------------------------------------------------------------------------
SELECT 'clients' AS table_metier, COUNT(*) AS lignes FROM clients
UNION ALL SELECT 'interactions', COUNT(*) FROM interactions
UNION ALL SELECT 'devis', COUNT(*) FROM devis
UNION ALL SELECT 'facture', COUNT(*) FROM facture
UNION ALL SELECT 'paiement', COUNT(*) FROM paiement;
