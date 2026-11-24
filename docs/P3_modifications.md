# Personne 3 : détail de toutes les modifications

Ce document liste, fichier par fichier, ce qui a été ajouté ou modifié, pourquoi, et comment le vérifier.
Les chemins sont relatifs à la racine du dépôt (branche `p3`). Rien n'a été déplacé : le backend reste à la racine
et le frontend dans `clientpulse-front/` (voir « Décisions »).

## Décisions

| Sujet | Décision | Raison |
|---|---|---|
| Arborescence `backend/` + `frontend/` | **Non appliquée**, mais script prêt : `tools/restructure-backend-frontend.sh` | Un `git mv` à J4 provoquerait des conflits chez P1 et P2. À lancer une fois tout fusionné dans `develop` (il met aussi à jour CI, Docker Compose, README et le générateur de démo ; testé sur une copie) |
| Sécurité | Session + CSRF conservée | Déjà en place (`SecurityConfig`), le README parlait à tort de JWT |
| `ddl-auto` | `validate` conservé | Garanti par `FlywaySchemaTest` ; documenté dans le README |
| Nom du service du tableau de bord | `DashboardService` (plan v3) | Le cahier des charges le nomme `KpiService` |
| Données de démo | `db/dev/V100` + profil `dev` | Les migrations métier restent sous V100 |

## 1. Build

| Fichier | Modification |
|---|---|
| `pom.xml` | **Ajouts** : `springdoc-openapi-starter-webmvc-ui` 3.0.0, `openpdf` 2.0.3 (`com.github.librepdf`), `opencsv` 5.9. **Retraits** : `testng` en version `RELEASE` (non reproductible, inutilisé), `spring-boot-starter-restdocs`, `spring-restdocs-mockmvc`, plugin `asciidoctor` (inutilisés) |
| `.gitignore` | Ajout de `.env` |

Points à vérifier à la première compilation : version de springdoc compatible avec Spring Boot 4.1.2 (3.0.x attendue)
et résolution d'OpenPDF 2.0.3 (paquet `com.lowagie.text`).

## 2. Base de données

| Fichier | Contenu |
|---|---|
| `db/migration/V7__journal_import.sql` | Table `journal_import` (fichier, lignes lues / importées / rejetées, colonnes d'audit) |
| `db/migration/V8__delais_relance.sql` | 5 paramètres `DELAI_RELANCE_N1` à `N5` (3, 3, 15, 30, 45) dans `parametres_scoring` |
| `db/dev/V100__demo_data.sql` | Données de démonstration (profil dev), générées par `tools/generate_demo_data.py` |
| `FlywaySchemaTest` | Versions attendues `1, 2, 4, 5, 6, 7, 8`, table `journal_import`, test des 5 délais |

## 3. Configuration

| Fichier | Modification |
|---|---|
| `application.properties` | Upload limité à 5 Mo, springdoc désactivé par défaut (`SPRINGDOC_ENABLED`), identité de l'émetteur des PDF (`app.entreprise.*`) |
| `application-dev.properties` (nouveau) | `spring.flyway.locations` ajoute `db/dev`, Swagger UI activé, logs DEBUG |
| `config/OpenApiConfig.java` (nouveau) | Métadonnées OpenAPI |
| `config/SecurityConfig.java` | `/api/import/**` ADMIN ; `/api/dashboard/**` ADMIN, MANAGER, ANALYSTE ; PDF facture ADMIN, MANAGER, COMPTABLE ; PDF devis tous sauf ANALYSTE ; routes OpenAPI autorisées (inexistantes hors profil dev) |
| `presentation/controller/GlobalExceptionHandler.java` | Nouveaux cas : accès refusé (403), fichier trop gros (413), filet de sécurité 500 sans trace technique (les erreurs Spring `ErrorResponse` restent traitées par Spring) |

## 4. Import CSV (C8)

| Fichier | Rôle |
|---|---|
| `domain/entity/JournalImport.java` | Entité du journal |
| `dao/interfaces/JournalImportDao.java`, `dao/jpa/JournalImportJpaRepository.java`, `dao/impl/JournalImportDaoImpl.java` | Accès aux données |
| `dto/RapportImportDto`, `LigneRejeteeDto`, `JournalImportDto` | Réponses de l'API |
| `service/interfaces/CsvImportService.java`, `service/impl/CsvImportServiceImpl.java` | Contrôle, nettoyage, import, journal |
| `presentation/controller/ImportController.java` | `POST /api/import/clients`, `GET /journal`, `GET /modele-clients` |
| `service/CsvImportServiceTest.java` | 9 tests |

Comportement : séparateur `;` ou `,` détecté, UTF-8 ou Windows-1252, BOM ignoré ; colonnes obligatoires `code`,
`raison_sociale`, `type` ; contrôles de longueur, e-mail, ICE (15 chiffres), délai 0 à 180, commercial existant,
doublon dans le fichier et en base ; nettoyage (espaces, casse, téléphone). Une ligne invalide est rejetée avec son motif
sans bloquer les autres.

## 5. Tableau de bord (C4, C5, C6)

| Fichier | Rôle |
|---|---|
| `dto/DashboardDto`, `PointMensuelDto`, `TrancheEncoursDto`, `ClientCaDto`, `SuccesRelancesDto` | Données du tableau de bord |
| `dao/interfaces/IndicateurDao.java`, `dao/impl/IndicateurDaoImpl.java` | Agrégats SQL (une requête par indicateur) |
| `service/interfaces/DashboardService.java`, `service/impl/DashboardServiceImpl.java` | Rétention, churn, CLV, DSO, recouvrement, succès des relances, séries, export CSV |
| `presentation/controller/DashboardController.java` | `GET /api/dashboard?periode=12`, `GET /api/dashboard/export` |
| `service/DashboardServiceTest.java` | 10 tests (formules, bornes, division par zéro, série mensuelle, CSV) |

Définitions : voir README section 9 et la Javadoc de `IndicateurDao` / `DashboardServiceImpl`.

## 6. PDF (B10)

| Fichier | Rôle |
|---|---|
| `dto/DocumentPdfDto.java` | Nom de fichier et contenu |
| `service/interfaces/PdfService.java`, `service/impl/PdfServiceImpl.java` | Devis, facture, avoir, brouillon : émetteur, client, lignes, TVA par taux, totaux, paiements, reste à payer |
| `presentation/controller/PdfController.java` | `GET /api/devis/{id}/pdf`, `GET /api/factures/{id}/pdf` |
| `service/impl/PdfServiceImplTest.java` | 7 tests (PDF valide, pagination, noms de fichier, TVA par taux, client inconnu) |

## 7. Délais de relance paramétrables (règle 7.5)

Modifie du code de P1 et P2, de façon rétrocompatible :

| Fichier | Modification |
|---|---|
| `service/impl/DelaisRelance.java` (nouveau) | Lit `DELAI_RELANCE_N1..N5`, valeurs par défaut si absentes |
| `service/impl/NiveauxRelance.java` | Surcharges `niveauAtteint(jours, decalages)` et `libelle(niveau, decalages)` ; anciennes méthodes inchangées |
| `service/impl/RelancePaiementServiceImpl.java` | Second constructeur (`@Autowired`) avec `DelaisRelance` ; le constructeur à 3 arguments est conservé pour les tests |
| `service/scoring/NomsParametres.java` | 5 constantes |
| `service/impl/ParametreScoringServiceImpl.java` | Les délais N2 à N5 doivent être strictement croissants |
| `service/DelaisRelanceTest.java`, `RelancePaiementServiceTest.java` | 4 tests + 1 test d'intégration |

## 8. Frontend

| Fichier | Rôle |
|---|---|
| `package.json` | Dépendance `chart.js` ^4.4.7 |
| `shared/ui/` | `page-header`, `alert`, `loading-state`, `empty-state`, `chart` (enveloppe Chart.js) |
| `shared/pipes/mad.pipe.ts` (+ spec) | Montants « 12 500,50 MAD » |
| `core/files/file-download.service.ts` (+ spec), `pdf.service.ts` | Téléchargement authentifié de PDF et CSV |
| `features/dashboard/` | Écran Tableau de bord : 8 indicateurs, 4 graphiques, tableau d'encours, export CSV |
| `features/import/` | Écran Import CSV : choix du fichier, compte rendu, rejets, historique, modèle |
| `features/parametres/` | Écran Paramètres : délais, poids (somme contrôlée), seuils, RFM |
| `*.spec.ts` des 3 écrans et de leurs services | 13 tests Vitest : appels HTTP, affichage, changement de période, refus du rôle, import avec rejets, enregistrement d'un paramètre, saisie invalide |
| `app.routes.ts` | Routes `/dashboard` (ADMIN, MANAGER, ANALYSTE), `/import` et `/parametres` (ADMIN) |
| `layout/shell/shell.component.html` | Liens Tableau de bord, Import CSV, Paramètres selon le rôle |

À faire côté P1 : brancher `PdfService` sur les boutons « PDF » des écrans devis et facture :
`this.pdf.ouvrirFacture(id).subscribe()`.

## 9. Livraison et outillage

| Fichier | Rôle |
|---|---|
| `.github/workflows/ci.yml` | Backend : `./mvnw verify` (Testcontainers) ; frontend : `ng build` + `ng test` |
| `Dockerfile`, `.dockerignore` | Image backend en deux étapes, utilisateur non root, fuseau Africa/Casablanca |
| `clientpulse-front/Dockerfile`, `nginx.conf`, `.dockerignore` | Build Angular et service nginx avec relais `/api` |
| `docker-compose.yml`, `.env.example` | MySQL 8.4, backend, frontend, phpMyAdmin optionnel |
| `tools/generate_demo_data.py` | Générateur des données de démonstration |
| `README.md` | Réécrit : Boot 4.1.2, session + CSRF, structure réelle, Docker, comptes, migrations, règles métier |
| `docs/jeu-de-donnees-demo.md`, `docs/livrable-2-corrections.md` | Jeu de démo et écarts de conception |
| `src/test/.../TestcontainersConfiguration.java` | `mysql:8.4` au lieu de `mysql:latest` |

## 10. Soutenance

Deck de 7 diapositives (problème, architecture, règles métier, démonstration, qualité, équipe) créé à part dans
Claude, au format « Slides » : il se présente, se partage et s'exporte en PowerPoint ou PDF depuis l'éditeur.
Les noms de l'équipe sont à remplacer (`[Nom Prénom 1..3]`).

## 11. Vérifications à faire (non exécutables dans l'environnement de rédaction)

Le code a été relu et contrôlé syntaxiquement (Java et TypeScript), le jeu de démo a été contrôlé contre le schéma
et exécuté dans SQLite, mais **rien n'a été compilé ni lancé** (pas de Maven ni de réseau). Ordre conseillé :

1. `./mvnw clean verify` : corriger d'éventuelles erreurs de compilation (imports, versions des dépendances).
2. `cd clientpulse-front && npm install && npm test -- --watch=false && npx ng build` (les 13 tests Angular des nouveaux écrans n'ont jamais été exécutés) : `npm install` met à jour `package-lock.json` (à committer, sinon `npm ci` échoue en CI).
3. `docker compose up --build`, puis le scénario de `docs/jeu-de-donnees-demo.md`.
4. Ouvrir un PDF de facture et de devis, importer le modèle CSV, changer un délai de relance.

Risques connus :
- les requêtes natives de `IndicateurDaoImpl` visent MySQL 8 : à tester sur la base de démo (écran Tableau de bord) ;
- le filet de sécurité 500 de `GlobalExceptionHandler` change le comportement des exceptions inattendues (réponse JSON, trace dans les logs) ;
- les écrans de P1 (devis, factures, produits, balance âgée) importent `core/api.services` et `core/models`, absents de cette branche : à récupérer auprès de P1 avant `ng build`.
