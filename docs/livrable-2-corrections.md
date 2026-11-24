# Corrections à reporter dans le Livrable 2

Écarts constatés entre la conception (cahier des charges v2.0, MLD, diagrammes de séquence) et le code livré.
À reporter dans le document de conception avant la soutenance, ou à présenter comme « évolutions justifiées ».

## Modèle de données

| Livrable 2 | Réalisé | Justification |
|---|---|---|
| `facture.reste_a_payer` stocké | **Non stocké** : calculé = total TTC − somme des paiements | Évite une donnée dérivée qui pourrait se désynchroniser |
| `paiement.mode` | Colonne `mode_paiement` (VIREMENT, CHEQUE, ESPECES, CARTE, EFFET) | `mode` est un mot réservé en SQL |
| `relance.affectee_a_id` (utilisateur) | `relances.commercial_id` (commercial) | Une relance est affectée à un commercial du portefeuille |
| Tables au singulier (`client`, `utilisateur`…) | Pluriel pour `clients`, `utilisateurs`, `commerciaux`, `relances`, `interactions`, `roles`, `utilisateurs_roles`, `parametres_scoring`, `scores_client` ; singulier pour `devis`, `facture`, `paiement`, `produit`, `taux_tva` | Convention Flyway du projet |
| `journal_import` absent des migrations initiales | Ajouté en **V7** (+ colonne `lignes_importees`) | Traçabilité de l'import CSV (C8) |
| Délais de relance codés en dur | Paramètres `DELAI_RELANCE_N1` à `N5` en **V8** | Règle 7.5 : « les délais sont paramétrables » |
| `contrat` | Non réalisé | Exigence A7 classée Could |
| `ligne_devis.produit_id`, `ligne_facture.produit_id`, `relances.facture_id`, `relances.niveau` | Ajoutés en V6 par `ALTER TABLE` | Lien catalogue et unicité d'une relance par facture, nature et niveau |

## Architecture et sécurité

| Livrable 2 | Réalisé |
|---|---|
| Spring Boot 3 | Spring Boot **4.1.2**, Java 17 |
| Thymeleaf (cahier des charges) | **Angular 21** (API REST + SPA), choix déjà acté dans l'équipe |
| Authentification JWT (ancien README) | **Session HTTP + jeton CSRF** (cookie `XSRF-TOKEN`, en-tête `X-XSRF-TOKEN`), BCrypt |
| Architecture à 4 couches Controller / Service / Repository / Entity | Présentation / Service / **DAO** (interfaces) / Domain, règles vérifiées par ArchUnit |

## Valeurs de statut utilisées par le code (diagrammes de séquence)

Les diagrammes de séquence proposaient des noms provisoires pour les statuts de relance. Les valeurs réelles sont :

| Diagramme | Valeur proposée | Valeur réelle |
|---|---|---|
| 10, 13 | `OUVERTE`, `AFFECTEE`, `TRAITEE`, `SUGGEREE` | `A_FAIRE`, `EN_COURS`, `TERMINEE` |
| 10 | Niveaux 0 à 4 | Niveaux **1 à 5** (1 = J-3, 2 = J+3, 3 = J+15, 4 = J+30, 5 = J+45) |
| 8 | Paiement sur facture | Le service refuse un paiement qui dépasse le reste à payer ; statut final `PAYEE`, `PARTIELLEMENT_PAYEE` ou `EN_RETARD` |

Autres écarts des diagrammes : les documents PDF sont servis par `/api/devis/{id}/pdf` et `/api/factures/{id}/pdf`
(`PdfService`, OpenPDF) ; l'import CSV par `/api/import/clients` (`CsvImportService`, OpenCSV) ; le tableau de bord par
`/api/dashboard` (`DashboardService`, nommé `KpiService` dans le cahier des charges).
