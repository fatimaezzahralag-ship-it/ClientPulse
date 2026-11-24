-- =============================================================================
-- ClientPulse - V8 : délais paramétrables des relances de paiement
--
-- Personne 3 - J4. Règle 7.5 du cahier des charges : "les délais sont paramétrables".
-- N1 = nombre de jours AVANT l'échéance (rappel préventif),
-- N2 à N5 = nombre de jours APRÈS l'échéance.
-- Valeurs par défaut = J-3, J+3, J+15, J+30, J+45 (comportement historique).
-- Modifiables depuis l'écran des paramètres (PUT /api/parametres/{id}).
-- =============================================================================

INSERT INTO parametres_scoring (nom, valeur) VALUES
    ('DELAI_RELANCE_N1',  3.0000),
    ('DELAI_RELANCE_N2',  3.0000),
    ('DELAI_RELANCE_N3', 15.0000),
    ('DELAI_RELANCE_N4', 30.0000),
    ('DELAI_RELANCE_N5', 45.0000);
