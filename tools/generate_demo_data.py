#!/usr/bin/env python3
"""
Génère src/main/resources/db/dev/V100__demo_data.sql : jeu de données de démonstration de ClientPulse.

    python3 tools/generate_demo_data.py            # régénère le fichier SQL
    python3 tools/generate_demo_data.py --check    # idem + vérifie la cohérence (SQLite en mémoire)

Le fichier est DÉTERMINISTE (graine fixe) : le relancer produit le même SQL. Les dates sont calculées par rapport à
la date d'ancrage ci-dessous (la démonstration finale), sur 24 mois d'historique.

Contenu (voir docs/jeu-de-donnees-demo.md) : 8 utilisateurs (mot de passe Demo@12345), 5 commerciaux, 12 produits,
90 clients de profils variés (fidèles, qui ralentissent, mauvais payeurs, ponctuels, perdus, nouveaux, prospects),
devis, factures numérotées sans trou, lignes, paiements (dont partiels), interactions, relances de paiement.

Les scores de risque et les segments RFM ne sont PAS insérés : ils sont calculés par le moteur
(bouton « Recalculer les scores » ou traitement de 02 h).
"""
import random
import re
import sqlite3
import sys
from collections import defaultdict
from datetime import date, datetime, timedelta
from decimal import Decimal, ROUND_HALF_UP
from pathlib import Path

ANCRE = date(2026, 11, 19)          # « aujourd'hui » de la démonstration
DEBUT = date(2024, 12, 1)           # début de l'historique (24 mois)
MOT_DE_PASSE = "Demo@12345"
# Hachage BCrypt de MOT_DE_PASSE (coût 10), constant pour que la génération reste déterministe.
HASH_DEMO = "$2b$10$BVEocolofuyV1MI/usb5i.K6cf3xfclO5oQRyyG1JOSiu1AC2tFwK"
SORTIE = Path(__file__).resolve().parent.parent / "src/main/resources/db/dev/V100__demo_data.sql"

rnd = random.Random(20261119)
D2 = Decimal("0.01")


def q(x):
    return Decimal(x).quantize(D2, rounding=ROUND_HALF_UP)


def sql(v):
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "1" if v else "0"
    if isinstance(v, (int, Decimal)):
        return str(v)
    if isinstance(v, datetime):
        return "'" + v.strftime("%Y-%m-%d %H:%M:%S") + "'"
    if isinstance(v, date):
        return "'" + v.isoformat() + "'"
    return "'" + str(v).replace("\\", "\\\\").replace("'", "''") + "'"


class Sortie:
    """Accumule des lignes par table et les écrit en INSERT multi-lignes."""

    def __init__(self):
        self.blocs = []

    def table(self, nom, colonnes, lignes, par=100, brut=()):
        lignes = list(lignes)
        for i in range(0, len(lignes), par):
            morceau = lignes[i:i + par]
            valeurs = []
            for l in morceau:
                cellules = []
                for c, v in zip(colonnes, l):
                    cellules.append(v.sql if hasattr(v, "sql") else sql(v))
                valeurs.append("    (" + ", ".join(cellules) + ")")
            self.blocs.append(f"INSERT INTO {nom} ({', '.join(colonnes)}) VALUES\n" + ",\n".join(valeurs) + ";\n")

    def commentaire(self, texte):
        self.blocs.append(f"\n-- {texte}\n")


class Brut:
    def __init__(self, texte):
        self.sql = texte


# ----------------------------------------------------------------------------- référentiels
UTILISATEURS = [
    (101, "Benjelloun", "Karim", "manager@clientpulse.ma", "MANAGER"),
    (102, "Alaoui", "Salma", "comptable@clientpulse.ma", "COMPTABLE"),
    (103, "Tazi", "Youssef", "analyste@clientpulse.ma", "ANALYSTE"),
    (104, "Idrissi", "Hajar", "commercial1@clientpulse.ma", "COMMERCIAL"),
    (105, "Berrada", "Omar", "commercial2@clientpulse.ma", "COMMERCIAL"),
    (106, "Chraibi", "Nadia", "commercial3@clientpulse.ma", "COMMERCIAL"),
    (107, "Fassi", "Mehdi", "commercial4@clientpulse.ma", "COMMERCIAL"),
    (108, "Lahlou", "Imane", "commercial5@clientpulse.ma", "COMMERCIAL"),
]
EQUIPES = ["Équipe Nord", "Équipe Nord", "Équipe Sud", "Équipe Sud", "Grands comptes"]
OBJECTIFS = [120000, 100000, 110000, 90000, 200000]

PRODUITS = [  # ref, désignation, prix HT, marge %, taux TVA
    ("PRD-001", "Abonnement CRM mensuel", 1200, 62, 20),
    ("PRD-002", "Licence annuelle ClientPulse", 9500, 70, 20),
    ("PRD-003", "Formation utilisateurs (jour)", 3500, 55, 20),
    ("PRD-004", "Audit de données clients", 6800, 48, 20),
    ("PRD-005", "Support premium annuel", 4200, 65, 20),
    ("PRD-006", "Intégration API", 12000, 42, 20),
    ("PRD-007", "Module analytique avancé", 7800, 68, 20),
    ("PRD-008", "Hébergement cloud mensuel", 850, 35, 14),
    ("PRD-009", "Maintenance corrective (heure)", 450, 50, 20),
    ("PRD-010", "Développement sur mesure (jour)", 4800, 40, 20),
    ("PRD-011", "Pack reporting trimestriel", 2400, 58, 10),
    ("PRD-012", "Guide pratique de la relation client", 180, 30, 0),
]

VILLES = ["Casablanca", "Rabat", "Marrakech", "Tanger", "Fès", "Agadir", "Meknès", "Oujda", "Kénitra", "Tétouan"]
SECTEURS = ["Commerce", "Industrie", "Logistique", "Services", "Santé", "Immobilier", "Agroalimentaire", "Textile", "BTP", "Technologie", "Énergie", "Transport"]
PREFIXES = ["Atlas", "Sahara", "Médina", "Anfa", "Océan", "Rif", "Souss", "Chaouia", "Doukkala", "Tamesna", "Zenata", "Ifrane", "Argan", "Kasbah", "Bouregreg", "Maghreb", "Nador", "Safi", "Essaouira", "Volubilis"]
SUFFIXES = ["Négoce", "Industries", "Logistique", "Conseil", "Distribution", "Services", "Technologies", "Agro", "BTP", "Textile", "Pharma", "Immobilier", "Énergie", "Transport"]
FORMES = ["SARL", "SA", "SARL AU"]
NOMS = ["Amrani", "Bennani", "El Fassi", "Guerraoui", "Haddad", "Kettani", "Mansouri", "Naciri", "Ouazzani", "Squalli", "Zniber", "Lamrani"]
PRENOMS = ["Aicha", "Brahim", "Chaimae", "Driss", "Fatima", "Hamza", "Khadija", "Laila", "Mounir", "Rachid", "Sanae", "Tarik"]
MODES = ["VIREMENT", "VIREMENT", "VIREMENT", "CHEQUE", "CHEQUE", "ESPECES", "CARTE", "EFFET"]

PROFILS = (["fidele"] * 25 + ["ralentit"] * 12 + ["mauvais"] * 10 + ["ponctuel"] * 15
           + ["perdu"] * 10 + ["nouveau"] * 8 + ["prospect"] * 10)


def slug(texte):
    t = texte.lower()
    for a, b in {"é": "e", "è": "e", "ê": "e", "à": "a", "ï": "i", "ô": "o", "ç": "c", " ": "", "'": ""}.items():
        t = t.replace(a, b)
    return re.sub(r"[^a-z0-9]", "", t)


# ----------------------------------------------------------------------------- génération
def dates_facturation(profil):
    """Dates d'émission d'un client selon son profil."""
    fin = ANCRE - timedelta(days=2)
    if profil == "fidele":
        debut, pas = DEBUT + timedelta(days=rnd.randint(0, 40)), (35, 60)
    elif profil == "ralentit":
        debut, pas = DEBUT + timedelta(days=rnd.randint(0, 60)), (30, 55)
    elif profil == "mauvais":
        debut, pas = DEBUT + timedelta(days=rnd.randint(30, 150)), (40, 75)
    elif profil == "ponctuel":
        debut, pas = ANCRE - timedelta(days=rnd.randint(250, 640)), (90, 200)
    elif profil == "perdu":
        debut, pas = DEBUT + timedelta(days=rnd.randint(0, 60)), (45, 90)
        fin = ANCRE - timedelta(days=rnd.randint(300, 440))
    elif profil == "nouveau":
        debut, pas = ANCRE - timedelta(days=rnd.randint(35, 80)), (20, 40)
    else:
        return []
    jours, d = [], debut
    while d <= fin:
        jours.append(d)
        d += timedelta(days=rnd.randint(*pas))
    if profil == "ponctuel":
        jours = jours[:4]
    return jours


def delai_paiement_reel(profil, emission, echeance):
    """Date de paiement complet, ou None si la facture reste impayée."""
    age = (ANCRE - emission).days
    if profil == "fidele":
        retard = rnd.randint(-8, 6)
    elif profil == "ralentit":
        if age < 130 and rnd.random() < 0.25:
            return None
        retard = rnd.randint(10, 38) if age < 190 else rnd.randint(-3, 6)
    elif profil == "mauvais":
        if age < 320 and rnd.random() < 0.50:
            return None
        retard = rnd.randint(25, 75)
    elif profil == "ponctuel":
        retard = rnd.randint(0, 20)
    elif profil == "perdu":
        retard = rnd.randint(-2, 10)
    else:
        retard = rnd.randint(0, 10)
    return echeance + timedelta(days=retard)


def main(verifier):
    hash_demo = HASH_DEMO

    out = Sortie()

    # ---------- utilisateurs, rôles, commerciaux
    out.commentaire("Utilisateurs de démonstration (mot de passe commun : " + MOT_DE_PASSE + ")")
    out.table("utilisateurs", ["id", "nom", "prenom", "email", "mot_de_passe", "actif"],
              [(i, n, p, e, hash_demo, True) for i, n, p, e, _ in UTILISATEURS])
    out.table("utilisateurs_roles", ["utilisateur_id", "role_id"],
              [(i, Brut(f"(SELECT id FROM roles WHERE nom = '{r}')")) for i, _, _, _, r in UTILISATEURS])
    commerciaux = [(k + 1, 104 + k, EQUIPES[k], OBJECTIFS[k]) for k in range(5)]
    out.table("commerciaux", ["id", "utilisateur_id", "equipe", "objectif_mensuel"], commerciaux)

    # ---------- produits
    out.commentaire("Catalogue produits (taux de TVA lus dans la table taux_tva de la migration V6)")
    out.table("produit", ["id", "reference", "designation", "prix_unitaire_ht", "marge", "actif", "taux_tva_id"],
              [(i + 1, ref, des, q(prix), q(marge), True,
                Brut(f"(SELECT id FROM taux_tva WHERE taux = {tva}.00 LIMIT 1)"))
               for i, (ref, des, prix, marge, tva) in enumerate(PRODUITS)])

    # ---------- clients
    profils = PROFILS[:]
    rnd.shuffle(profils)
    clients = []
    utilises = set()
    for i, profil in enumerate(profils, start=1):
        particulier = i % 9 == 0
        if particulier:
            nom = f"{rnd.choice(PRENOMS)} {rnd.choice(NOMS)}"
            while nom in utilises:
                nom = f"{rnd.choice(PRENOMS)} {rnd.choice(NOMS)}"
            ice, type_ = None, "PARTICULIER"
            delai = rnd.choice([0, 0, 15, 30])
        else:
            nom = f"{rnd.choice(PREFIXES)} {rnd.choice(SUFFIXES)} {rnd.choice(FORMES)}"
            while nom in utilises:
                nom = f"{rnd.choice(PREFIXES)} {rnd.choice(SUFFIXES)} {rnd.choice(FORMES)}"
            ice, type_ = "00" + "".join(str(rnd.randint(0, 9)) for _ in range(13)), "ENTREPRISE"
            delai = rnd.choice([30, 30, 45, 45, 60])
        utilises.add(nom)
        ville = rnd.choice(VILLES)
        statut = "PROSPECT" if profil == "prospect" else ("INACTIF" if profil == "perdu" and rnd.random() < 0.6 else "ACTIF")
        commercial = None if i % 15 == 0 else (i % 5) + 1
        courriel = f"contact@{slug(nom)[:22]}.ma"
        tel = "+2125" + "".join(str(rnd.randint(0, 9)) for _ in range(8))
        clients.append(dict(id=i, code=f"CLI-{i:04d}", nom=nom, ice=ice, type=type_, email=courriel, tel=tel,
                            adresse=f"{rnd.randint(1, 180)} rue {rnd.choice(PREFIXES)}", ville=ville,
                            secteur=rnd.choice(SECTEURS), delai=delai, statut=statut, commercial=commercial,
                            profil=profil))
    out.commentaire("Clients : profils variés pour illustrer la balance âgée, le RFM et le score de risque")
    out.table("clients",
              ["id", "code", "raison_sociale", "ice", "email", "telephone", "adresse", "ville", "secteur",
               "delai_paiement_jours", "type", "statut", "commercial_id"],
              [(c["id"], c["code"], c["nom"], c["ice"], c["email"], c["tel"], c["adresse"], c["ville"], c["secteur"],
                c["delai"], c["type"], c["statut"], c["commercial"]) for c in clients])

    # ---------- factures (émises) : lignes, montants, dates
    def construire_lignes():
        lignes, ht, tva_total = [], Decimal(0), Decimal(0)
        for p in rnd.sample(range(len(PRODUITS)), rnd.choice([1, 1, 2, 2, 3, 4])):
            ref, des, prix, marge, taux = PRODUITS[p]
            quantite = Decimal(rnd.choice([1, 1, 1, 2, 3, 5, 10 if prix < 1500 else 1]))
            prix_u = q(Decimal(prix) * Decimal(rnd.choice([100, 100, 95, 105])) / 100)
            remise = Decimal(rnd.choice([0, 0, 0, 5, 10]))
            montant_ht = q(quantite * prix_u * (Decimal(100) - remise) / 100)
            montant_tva = q(montant_ht * Decimal(taux) / 100)
            ht += montant_ht
            tva_total += montant_tva
            lignes.append(dict(produit=p + 1, designation=des, quantite=q(quantite), prix=prix_u, remise=q(remise),
                               taux=q(taux), ht=montant_ht, tva=montant_tva))
        return lignes, ht, tva_total

    factures = []
    for c in clients:
        for emission in dates_facturation(c["profil"]):
            lignes, ht, tva = construire_lignes()
            echeance = emission + timedelta(days=c["delai"])
            factures.append(dict(client=c, emission=emission, echeance=echeance, lignes=lignes, ht=ht, tva=tva,
                                 ttc=ht + tva, profil=c["profil"]))
    factures.sort(key=lambda f: (f["emission"], f["client"]["id"]))

    # numérotation continue, par année, sans trou
    compteur = defaultdict(int)
    for i, f in enumerate(factures, start=1):
        f["id"] = i
        compteur[f["emission"].year] += 1
        f["numero"] = f"FAC-{f['emission'].year}-{compteur[f['emission'].year]:04d}"
    seq_facture = dict(compteur)

    # ---------- paiements et statuts
    paiements = []
    for f in factures:
        f["paiements"] = []
        total_paye = Decimal(0)
        date_complete = delai_paiement_reel(f["profil"], f["emission"], f["echeance"])
        if date_complete is not None:
            date_complete = max(date_complete, f["emission"] + timedelta(days=1))   # jamais avant l'émission
        if date_complete is not None and date_complete <= ANCRE:
            if rnd.random() < 0.12 and (date_complete - f["emission"]).days >= 6:   # deux paiements : 60 % puis 40 %
                premier = q(f["ttc"] * Decimal("0.6"))
                d1 = max(f["emission"] + timedelta(days=2), date_complete - timedelta(days=rnd.randint(3, 25)))
                f["paiements"] = [(d1, premier), (date_complete, f["ttc"] - premier)]
            else:
                f["paiements"] = [(date_complete, f["ttc"])]
        elif f["profil"] in ("mauvais", "ralentit") and rnd.random() < 0.35 and (ANCRE - f["emission"]).days > 30:
            part = q(f["ttc"] * Decimal(rnd.choice(["0.3", "0.5"])))
            f["paiements"] = [(min(f["emission"] + timedelta(days=rnd.randint(5, 25)), ANCRE), part)]
        for d, m in f["paiements"]:
            total_paye += m
        f["paye"] = total_paye
        reste = f["ttc"] - total_paye
        if reste == 0:
            f["statut"] = "PAYEE"
        elif f["echeance"] < ANCRE:
            f["statut"] = "EN_RETARD"
        elif total_paye > 0:
            f["statut"] = "PARTIELLEMENT_PAYEE"
        else:
            f["statut"] = "EMISE"
        for d, m in f["paiements"]:
            paiements.append((f["id"], d, m))

    # ---------- devis : un devis accepté pour ~45 % des factures, plus des devis en cours
    devis = []
    for f in factures:
        if rnd.random() < 0.45 and f["client"]["commercial"]:
            d_emission = f["emission"] - timedelta(days=rnd.randint(4, 22))
            devis.append(dict(client=f["client"], emission=d_emission, validite=d_emission + timedelta(days=30),
                              statut="ACCEPTE", lignes=f["lignes"], ht=f["ht"], tva=f["tva"], ttc=f["ttc"], facture=f))
    actifs = [c for c in clients if c["profil"] not in ("perdu",) and c["commercial"]]
    for statut, nombre, jours in [("ENVOYE", 14, (3, 25)), ("BROUILLON", 8, (0, 10)), ("REFUSE", 8, (30, 200)), ("EXPIRE", 6, (60, 220))]:
        for _ in range(nombre):
            c = rnd.choice(actifs)
            d_emission = ANCRE - timedelta(days=rnd.randint(*jours))
            lignes, ht, tva = construire_lignes()
            devis.append(dict(client=c, emission=d_emission, validite=d_emission + timedelta(days=30),
                              statut=statut, lignes=lignes, ht=ht, tva=tva, ttc=ht + tva, facture=None))
    devis.sort(key=lambda d: (d["emission"], d["client"]["id"]))
    compteur_d = defaultdict(int)
    for i, d in enumerate(devis, start=1):
        d["id"] = i
        compteur_d[d["emission"].year] += 1
        d["numero"] = f"DEV-{d['emission'].year}-{compteur_d[d['emission'].year]:04d}"
        if d["facture"]:
            d["facture"]["devis_id"] = i

    out.commentaire("Devis (numérotation DEV-AAAA-NNNN continue par année)")
    out.table("devis", ["id", "numero", "date_emission", "date_validite", "statut", "total_ht", "total_tva", "total_ttc",
                        "client_id", "commercial_id", "date_creation", "date_modification", "cree_par", "modifie_par"],
              [(d["id"], d["numero"], d["emission"], d["validite"], d["statut"], d["ht"], d["tva"], d["ttc"],
                d["client"]["id"], d["client"]["commercial"],
                datetime.combine(d["emission"], datetime.min.time()).replace(hour=9),
                datetime.combine(d["emission"], datetime.min.time()).replace(hour=9), "demo", "demo") for d in devis])
    out.table("ligne_devis", ["devis_id", "produit_id", "designation", "quantite", "prix_unitaire_ht", "remise", "taux_tva"],
              [(d["id"], l["produit"], l["designation"], l["quantite"], l["prix"], l["remise"], l["taux"])
               for d in devis for l in d["lignes"]], par=200)

    # ---------- brouillons de facture (non numérotés, sans date d'émission)
    brouillons = []
    for c in rnd.sample([c for c in clients if c["profil"] in ("fidele", "nouveau")], 4):
        lignes, ht, tva = construire_lignes()
        brouillons.append(dict(id=len(factures) + len(brouillons) + 1, client=c, lignes=lignes, ht=ht, tva=tva, ttc=ht + tva))

    out.commentaire("Factures émises (numérotation FAC-AAAA-NNNN continue par année) et 4 brouillons")
    out.table("facture", ["id", "numero", "type", "date_emission", "date_echeance", "statut", "total_ht", "total_tva",
                          "total_ttc", "client_id", "devis_id", "facture_origine_id", "date_creation",
                          "date_modification", "cree_par", "modifie_par"],
              [(f["id"], f["numero"], "FACTURE", f["emission"], f["echeance"], f["statut"], f["ht"], f["tva"], f["ttc"],
                f["client"]["id"], f.get("devis_id"), None,
                datetime.combine(f["emission"], datetime.min.time()).replace(hour=10),
                datetime.combine(f["emission"], datetime.min.time()).replace(hour=10), "demo", "demo") for f in factures]
              + [(b["id"], None, "FACTURE", None, None, "BROUILLON", b["ht"], b["tva"], b["ttc"], b["client"]["id"], None, None,
                  datetime.combine(ANCRE - timedelta(days=1), datetime.min.time()).replace(hour=11),
                  datetime.combine(ANCRE - timedelta(days=1), datetime.min.time()).replace(hour=11), "demo", "demo")
                 for b in brouillons])
    out.table("ligne_facture", ["facture_id", "produit_id", "designation", "quantite", "prix_unitaire_ht", "remise",
                                "taux_tva", "montant_ht", "montant_tva"],
              [(f["id"], l["produit"], l["designation"], l["quantite"], l["prix"], l["remise"], l["taux"], l["ht"], l["tva"])
               for f in factures + brouillons for l in f["lignes"]], par=200)

    out.commentaire("Paiements, dont paiements partiels")
    out.table("paiement", ["facture_id", "date_paiement", "montant", "mode_paiement", "reference", "date_creation",
                           "date_modification", "cree_par", "modifie_par"],
              [(fid, d, m, rnd.choice(MODES), f"PAY-{i:05d}", datetime.combine(d, datetime.min.time()).replace(hour=15),
                datetime.combine(d, datetime.min.time()).replace(hour=15), "demo", "demo")
               for i, (fid, d, m) in enumerate(paiements, start=1)], par=200)

    # ---------- interactions
    types = ["APPEL", "APPEL", "EMAIL", "EMAIL", "REUNION"]
    resumes = {
        "APPEL": ["Appel de suivi : le client confirme sa satisfaction.", "Appel pour une demande de devis complémentaire.",
                  "Appel de relance sur une facture en attente.", "Le client demande une démonstration du module analytique."],
        "EMAIL": ["E-mail d'envoi de la proposition commerciale.", "E-mail de confirmation de commande.",
                  "Réponse à une question sur la facturation.", "Envoi de la documentation produit."],
        "REUNION": ["Réunion de cadrage du projet d'intégration.", "Revue trimestrielle de la relation client.",
                    "Réunion de présentation des nouvelles offres."],
        "RECLAMATION": ["Réclamation : erreur sur une ligne de facture.", "Réclamation : délai de livraison dépassé.",
                        "Réclamation : le support n'a pas répondu à temps."],
    }
    interactions = []
    for c in clients:
        if c["profil"] == "prospect":
            nombre = rnd.randint(0, 2)
        elif c["profil"] in ("fidele", "nouveau"):
            nombre = rnd.randint(2, 6)
        elif c["profil"] == "perdu":
            nombre = rnd.randint(0, 2)
        else:
            nombre = rnd.randint(1, 4)
        for _ in range(nombre):
            t = rnd.choice(types)
            if c["profil"] in ("mauvais", "ralentit") and rnd.random() < 0.30:
                t = "RECLAMATION"
            jours = rnd.randint(2, 540 if c["profil"] != "perdu" else 600)
            jour = ANCRE - timedelta(days=jours)
            if c["profil"] == "perdu":
                jour = ANCRE - timedelta(days=rnd.randint(330, 600))
            if t == "RECLAMATION":
                statut = "OUVERTE" if jours < 60 and rnd.random() < 0.6 else "CLOTUREE"
            else:
                statut = "CLOTUREE" if jours > 20 else rnd.choice(["OUVERTE", "EN_COURS", "CLOTUREE"])
            interactions.append((c["id"], datetime.combine(jour, datetime.min.time()).replace(hour=rnd.randint(8, 17)),
                                 rnd.choice(resumes[t]), statut, t))
    out.commentaire("Interactions (appels, e-mails, réunions, réclamations)")
    out.table("interactions", ["client_id", "`date`", "resume", "statut", "type"],
              [(cid, dt, r, s, t) for cid, dt, r, s, t in interactions], par=200)

    # ---------- relances de paiement cohérentes avec les retards constatés
    relances = []
    for f in factures:
        c = f["client"]
        if f["statut"] == "PAYEE" and f["paiements"] and (ANCRE - f["emission"]).days < 270:
            dernier = f["paiements"][-1][0]
            retard = (dernier - f["echeance"]).days
            for niveau, seuil in ((2, 3), (3, 15)):
                if retard > seuil:
                    creation = f["echeance"] + timedelta(days=seuil)
                    reussie = (dernier - creation).days <= 30
                    relances.append((c["id"], c["commercial"], "PAIEMENT", "TERMINEE",
                                     f"Relance J+{seuil} : facture {f['numero']}, échéance le {f['echeance']}.",
                                     "AUTRE" if reussie else "SANS_REPONSE", "Relance traitée.", creation, creation, f["id"], niveau))
        elif f["statut"] == "EN_RETARD":
            jours = (ANCRE - f["echeance"]).days
            niveau = 5 if jours >= 45 else 4 if jours >= 30 else 3 if jours >= 15 else 2 if jours >= 3 else 0
            if niveau:
                seuil = {2: 3, 3: 15, 4: 30, 5: 45}[niveau]
                creation = f["echeance"] + timedelta(days=seuil)
                relances.append((c["id"], c["commercial"], "PAIEMENT", "A_FAIRE",
                                 f"Relance J+{seuil} : facture {f['numero']}, échéance le {f['echeance']}.",
                                 None, None, ANCRE, creation, f["id"], niveau))
    # relances commerciales terminées (rétention), pour le taux de succès
    for c in [c for c in clients if c["profil"] in ("ralentit", "perdu", "ponctuel") and c["commercial"]][:14]:
        creation = ANCRE - timedelta(days=rnd.randint(40, 200))
        relances.append((c["id"], c["commercial"], "COMMERCIALE", "TERMINEE",
                         "Client à risque : proposer un rendez-vous de fidélisation.",
                         rnd.choice(["RENDEZ_VOUS_PRIS", "INTERESSE", "SANS_REPONSE", "PAS_INTERESSE"]),
                         "Relance commerciale traitée.", creation, creation, None, None))
    out.commentaire("Relances : historique de paiement traité et relances à faire sur les retards actuels")
    out.table("relances", ["client_id", "commercial_id", "nature", "statut", "motif", "resultat", "commentaire",
                           "date_echeance", "date_creation", "facture_id", "niveau", "cree_par"],
              [(cid, com, nat, st, mot, res, com2, de, datetime.combine(dc, datetime.min.time()).replace(hour=9),
                fid, niv, "demo")
               for cid, com, nat, st, mot, res, com2, de, dc, fid, niv in
               [(r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8], r[9], r[10]) for r in relances]])

    # ---------- séquences de numérotation (dernier numéro utilisé par année)
    seq = [("FACTURE", a, n) for a, n in sorted(seq_facture.items())] + [("DEVIS", a, n) for a, n in sorted(compteur_d.items())]
    out.commentaire("Séquences de numérotation : le prochain numéro émis par l'application suit directement")
    out.table("sequence_numerotation", ["type", "annee", "dernier_numero"], seq)

    en_tete = (
        "-- =============================================================================\n"
        "-- ClientPulse - V100 : jeu de données de démonstration (profil dev uniquement)\n"
        "--\n"
        f"-- GÉNÉRÉ par tools/generate_demo_data.py (ancrage : {ANCRE.isoformat()}). Ne pas modifier à la main.\n"
        "-- Chargé uniquement avec spring.profiles.active=dev (voir application-dev.properties).\n"
        "-- Les migrations métier restent numérotées sous V100.\n"
        f"-- Mot de passe de tous les comptes de démo : {MOT_DE_PASSE}\n"
        "-- =============================================================================\n\n")
    SORTIE.write_text(en_tete + "\n".join(out.blocs), encoding="utf-8")

    stats = dict(clients=len(clients), devis=len(devis), factures=len(factures), brouillons=len(brouillons),
                 paiements=len(paiements), interactions=len(interactions), relances=len(relances))
    par_statut = defaultdict(int)
    for f in factures:
        par_statut[f["statut"]] += 1
    print("Écrit :", SORTIE, f"({SORTIE.stat().st_size // 1024} Ko)")
    print("Volumes :", stats)
    print("Factures par statut :", dict(par_statut))

    if verifier:
        verifier_coherence(factures, paiements, devis, clients)


def verifier_coherence(factures, paiements, devis, clients):
    # 1. numérotation continue par année
    par_annee = defaultdict(list)
    for f in factures:
        par_annee[f["emission"].year].append(int(f["numero"].rsplit("-", 1)[1]))
    for annee, nums in par_annee.items():
        assert nums == list(range(1, len(nums) + 1)), f"trou de numérotation en {annee}"
    # 2. totaux = somme des lignes ; paiements jamais supérieurs au total ; dates cohérentes
    for f in factures:
        assert f["ht"] == sum(l["ht"] for l in f["lignes"]) and f["ttc"] == f["ht"] + f["tva"]
        assert sum(m for _, m in f["paiements"]) <= f["ttc"]
        assert all(d >= f["emission"] and d <= ANCRE for d, _ in f["paiements"]), f["numero"]
        assert f["emission"] <= ANCRE
    # 3. un devis accepté ne sert qu'une fois
    ids = [f.get("devis_id") for f in factures if f.get("devis_id")]
    assert len(ids) == len(set(ids))
    # 4. exécution du SQL dans SQLite (syntaxe, nombre de colonnes, guillemets)
    texte = SORTIE.read_text(encoding="utf-8")
    cx = sqlite3.connect(":memory:")
    cx.execute("CREATE TABLE roles (id INTEGER PRIMARY KEY, nom TEXT)")
    cx.executemany("INSERT INTO roles (nom) VALUES (?)", [(n,) for n in ["ADMIN", "MANAGER", "COMMERCIAL", "COMPTABLE", "ANALYSTE"]])
    cx.execute("CREATE TABLE taux_tva (id INTEGER PRIMARY KEY, taux REAL)")
    cx.executemany("INSERT INTO taux_tva (taux) VALUES (?)", [(20.0,), (14.0,), (10.0,), (7.0,), (0.0,)])
    for table, colonnes in re.findall(r"INSERT INTO (\w+) \(([^)]*)\) VALUES", texte):
        if table not in ("roles", "taux_tva"):
            cx.execute(f"CREATE TABLE IF NOT EXISTS {table} ({colonnes})")
    sans_commentaires = "\n".join(l for l in texte.splitlines() if not l.startswith("--"))
    cx.executescript(sans_commentaires.replace("\\'", "''"))
    for table in ("clients", "devis", "facture", "ligne_facture", "paiement", "interactions", "relances"):
        n = cx.execute(f"SELECT COUNT(*) FROM {table}").fetchone()[0]
        assert n > 0, table
    # 5. un devis converti a des totaux égaux à ceux de sa facture
    ecarts = cx.execute("SELECT COUNT(*) FROM facture f JOIN devis d ON d.id = f.devis_id WHERE d.total_ttc <> f.total_ttc").fetchone()[0]
    assert ecarts == 0
    print("Vérifications OK : numérotation, totaux, paiements, devis, exécution SQL")


if __name__ == "__main__":
    main("--check" in sys.argv)
