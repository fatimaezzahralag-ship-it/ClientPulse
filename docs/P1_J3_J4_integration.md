# P1 J3/J4 : facturation, catalogue, paiements, relances de paiement

Intégré sur `main` après les livraisons de P2 (relances, scoring) et P3 (V1, exceptions, sécurité).

## Ce qui a été adapté pour s'aligner sur main
- **Relance** : une seule entité, celle de P2 (table `relances`). P1 y ajoute deux champs facultatifs, `facture` et `niveau`, utilisés seulement par les relances de nature PAIEMENT. `RelanceDao` reçoit une méthode, `existePourFactureEtNiveau`.
- **Migration V6** (`V6__catalogue_et_relances_paiement.sql`) : tables `taux_tva` et `produit` (avec 5 taux de TVA marocains), `produit_id` sur les lignes de devis et de facture, `facture_id` et `niveau` sur `relances`, contrainte unique (facture, nature, niveau). V3 reste libre. V1 de P3 n'est pas modifié.
- **RessourceIntrouvableException** : ajout de la fabrique `de(objet, id)`.
- **FlywaySchemaTest** : attend désormais la version 6 et les tables `taux_tva` et `produit`.
- Pas de second gestionnaire d'erreurs ni de sécurité temporaire : on garde `GlobalExceptionHandler` et `SecurityConfig` de main.

## Endpoints ajoutés
`/api/taux-tva`, `/api/produits`, `/api/devis` (+ `/{id}/envoi|acceptation|refus`), `/api/factures` (+ `/depuis-devis/{devisId}`, `/{id}/emission`, `/{id}/annulation`, `/balance-agee`), `/api/factures/{id}/paiements`, `POST /api/relances/paiement/generer?date=`.
Le suivi des relances (liste, démarrer, traiter) reste dans `RelanceController` de P2.

## Job
`RelancePaiementJob` tourne chaque nuit à 02h00 (Africa/Casablanca) : factures en retard, puis relances de paiement (J-3, J+3, J+15, J+30, J+45, un seul niveau par facture).
