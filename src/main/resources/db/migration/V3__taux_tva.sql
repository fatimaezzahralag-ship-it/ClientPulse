-- V3 : taux de TVA de départ (Maroc). La table taux_tva est créée par V1.
-- Les taux sont paramétrables ensuite via /api/taux-tva.
INSERT INTO taux_tva (libelle, taux, actif) VALUES
    ('TVA 20 %', 20.00, TRUE),
    ('TVA 14 %', 14.00, TRUE),
    ('TVA 10 %', 10.00, TRUE),
    ('TVA 7 %',   7.00, TRUE),
    ('Exonéré (0 %)', 0.00, TRUE);
