# Jeu de données de démonstration

Fichier : `src/main/resources/db/dev/V100__demo_data.sql`, **généré** par `tools/generate_demo_data.py`
(déterministe : relancer le script reproduit exactement le même SQL). Chargé uniquement avec le profil `dev`.

```bash
python3 tools/generate_demo_data.py            # régénère le SQL
python3 tools/generate_demo_data.py --check    # + vérifie la cohérence (SQLite en mémoire)
```

Les dates sont calculées par rapport au **19 novembre 2026** (ancrage du script), sur 24 mois d'historique
(décembre 2024 à novembre 2026). Pour décaler l'ancrage, modifier `ANCRE` et `DEBUT` en tête du script.

## Volumes

| Donnée | Quantité |
|---|---|
| Utilisateurs | 8 (manager, comptable, analyste, 5 commerciaux), mot de passe `Demo@12345` |
| Produits | 12 (taux de TVA 20 %, 14 %, 10 %, 0 %) |
| Clients | 90 |
| Devis | 391 (acceptés, envoyés, brouillons, refusés, expirés) |
| Factures émises | 822 : 718 payées, 47 en retard, 56 émises, 1 partiellement payée |
| Brouillons de facture | 4 (sans numéro, sans date d'émission) |
| Paiements | 810, dont des paiements partiels (60 % puis 40 %) |
| Interactions | 261 (appels, e-mails, réunions, réclamations) |
| Relances | 199 : relances de paiement traitées, relances à faire sur les retards actuels, relances commerciales terminées |

## Profils de clients

| Profil | Nombre | Comportement |
|---|---|---|
| Fidèles | 25 | Une facture toutes les 5 à 8 semaines, payées à l'échéance |
| Qui ralentissent | 12 | Réguliers, mais retards de paiement croissants sur les 6 derniers mois |
| Mauvais payeurs | 10 | Paient 25 à 75 jours après l'échéance, factures anciennes impayées |
| Ponctuels | 15 | 1 à 4 factures sur la période |
| Perdus | 10 | Plus aucune facture depuis 10 à 15 mois (certains inactifs) |
| Nouveaux | 8 | Première facture dans les 3 derniers mois |
| Prospects | 10 | Aucune facture |

Chaque jeu de profils alimente un écran : balance âgée (mauvais payeurs), segments RFM (fidèles, perdus, nouveaux),
score de risque avec facteurs explicatifs (ralentissent, réclamations ouvertes), rétention et churn (perdus).

## Garanties vérifiées par `--check`

- numérotation `FAC-AAAA-NNNN` et `DEV-AAAA-NNNN` continue, sans trou, par année ;
- total HT d'une facture = somme de ses lignes, TTC = HT + TVA ;
- jamais plus payé que le total TTC ; aucun paiement avant l'émission ni dans le futur ;
- un devis converti ne sert qu'une fois, et ses totaux égalent ceux de sa facture ;
- le SQL s'exécute (nombre de colonnes, guillemets) ;
- colonnes et contraintes `NOT NULL` conformes aux migrations V1 à V8 (contrôle fait lors de la livraison).

Les `sequence_numerotation` sont initialisées au dernier numéro utilisé : la prochaine facture émise dans
l'application continue la numérotation sans trou.

## Non inséré volontairement

Les tables `scores_client` (scores de risque, segments RFM) : elles sont calculées par le moteur.
Après le démarrage, ouvrir **Analytics** et lancer le recalcul (ou attendre le traitement de nuit).

## Scénario de démonstration

1. **Comptable** (`comptable@clientpulse.ma`) : ouvrir un devis accepté sans facture, le convertir en facture, l'émettre (numéro continu), télécharger le PDF.
2. Enregistrer un **paiement partiel** : le reste à payer et le statut changent.
3. **Balance âgée** : les mauvais payeurs apparaissent dans les tranches 31 à 60, 61 à 90 et plus de 90 jours.
4. **Admin** (`admin@clientpulse.ma`) : écran **Paramètres**, changer un délai de relance ; **Import CSV**, charger le modèle puis un fichier avec une erreur volontaire.
5. **Manager** (`manager@clientpulse.ma`) : **Tableau de bord** (trésorerie, rétention, CLV, relances), puis **Analytics** : segments RFM, clients à risque, facteurs explicatifs dont les retards de paiement.
6. Créer et affecter une relance commerciale ; se connecter en **commercial** pour la traiter.
