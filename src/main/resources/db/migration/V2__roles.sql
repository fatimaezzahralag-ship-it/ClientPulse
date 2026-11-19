-- Rôles applicatifs.
-- ON DUPLICATE KEY UPDATE rend la migration rejouable sur une base créée
-- auparavant par Hibernate (ddl-auto=update), où les rôles peuvent déjà exister
-- via DataInitializer. Sans cela, Flyway échouerait sur la clé unique uk_roles_nom.
INSERT INTO roles (nom) VALUES
    ('ADMIN'),
    ('MANAGER'),
    ('COMMERCIAL'),
    ('COMPTABLE'),
    ('ANALYSTE')
ON DUPLICATE KEY UPDATE nom = nom;
