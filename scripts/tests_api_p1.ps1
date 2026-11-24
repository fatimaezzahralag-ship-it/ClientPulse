<#
 ClientPulse - tests automatiques de l'API de P1 (catalogue, devis, factures, paiements, relances, balance agee).

 Utilisation (backend demarre sur le port 8080, base vide ou non) :
     .\tests_api_p1.ps1
     .\tests_api_p1.ps1 -Base http://localhost:8080 -Email admin@clientpulse.ma -Password "Admin@12345"

 Le script cree ses propres donnees (produit, client, devis, factures) avec un suffixe unique : on peut le relancer.
 Il ecrit OK ou FAIL pour chaque verification et un bilan a la fin. Il ne modifie aucun fichier.
 Attention : la partie "voyage dans le temps" appelle le traitement de nuit avec une date future,
 ce qui passe des factures en retard dans la base : a faire sur une base de developpement.
#>
param(
    [string]$Base = "http://localhost:8080",
    [string]$Email = "admin@clientpulse.ma",
    [string]$Password = "Admin@12345"
)

$ErrorActionPreference = "Stop"
$script:ok = 0
$script:ko = 0
$script:session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$run = Get-Date -Format "HHmmss"
$today = (Get-Date).ToString("yyyy-MM-dd")

# ------------------------------------------------------------------ outils

function Get-Cookie([string]$name) {
    $c = $script:session.Cookies.GetCookies($Base) | Where-Object { $_.Name -eq $name } | Select-Object -First 1
    if ($c) { return $c.Value }
    return $null
}

# Envoie une requete et renvoie Status (code HTTP), Json (corps decode) et Raw (corps brut), meme pour une erreur 4xx/5xx.
function Call {
    param([string]$Method, [string]$Path, $Body = $null, [bool]$Csrf = $true)
    $headers = @{ "Accept" = "application/json" }
    if ($Method -ne "GET" -and $Csrf) {
        $t = Get-Cookie "XSRF-TOKEN"
        if ($t) { $headers["X-XSRF-TOKEN"] = $t }
    }
    $req = @{ Uri = "$Base$Path"; Method = $Method; Headers = $headers; WebSession = $script:session; UseBasicParsing = $true }
    if ($null -ne $Body) {
        $req["Body"] = ($Body | ConvertTo-Json -Depth 10)
        $req["ContentType"] = "application/json; charset=utf-8"
    }
    $status = 0
    $content = ""
    try {
        $r = Invoke-WebRequest @req
        $status = [int]$r.StatusCode
        $content = $r.Content
    } catch {
        $resp = $_.Exception.Response
        if ($null -eq $resp) { throw }
        $status = [int]$resp.StatusCode
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
            $content = $_.ErrorDetails.Message
        } else {
            try { $content = (New-Object IO.StreamReader($resp.GetResponseStream())).ReadToEnd() } catch { }
        }
    }
    $json = $null
    if ($content) { try { $json = $content | ConvertFrom-Json } catch { } }
    return [pscustomobject]@{ Status = $status; Json = $json; Raw = $content }
}

function Check([string]$name, [bool]$cond, [string]$detail = "") {
    if ($cond) { $script:ok++; Write-Host "  OK   $name" -ForegroundColor Green }
    else { $script:ko++; Write-Host "  FAIL $name   $detail" -ForegroundColor Red }
}

function Titre([string]$t) { Write-Host ""; Write-Host "== $t" -ForegroundColor Cyan }

function Dec($v) { return [decimal]$v }

# Cree un devis d'une ligne libre, le fait accepter, cree la facture, puis l'emet si $emettre est vrai.
function Nouvelle-Facture($clientId, $prix, $emission, $echeance, [bool]$emettre = $true) {
    $d = Call POST "/api/devis" @{ clientId = $clientId; lignes = @(@{ designation = "Prestation $run"; quantite = 1; prixUnitaireHT = $prix; tauxTVA = 20 }) }
    Call POST "/api/devis/$($d.Json.id)/envoi" | Out-Null
    Call POST "/api/devis/$($d.Json.id)/acceptation" | Out-Null
    $f = Call POST "/api/factures/depuis-devis/$($d.Json.id)"
    if ($emettre) {
        $corps = @{}
        if ($emission) { $corps["dateEmission"] = $emission }
        if ($echeance) { $corps["dateEcheance"] = $echeance }
        $f = Call POST "/api/factures/$($f.Json.id)/emission" $corps
    }
    return $f.Json
}

# ------------------------------------------------------------------ 0. securite

Titre "0. Securite : sans connexion, avec et sans jeton CSRF"
$r = Call GET "/api/produits"
Check "GET /api/produits sans connexion -> 401" ($r.Status -eq 401) "statut recu $($r.Status)"

Call GET "/api/auth/csrf" | Out-Null
$r = Call POST "/api/auth/login" @{ email = $Email; motDePasse = "mauvais-mot-de-passe" }
Check "login avec un mauvais mot de passe -> 401" ($r.Status -eq 401) "statut recu $($r.Status)"

$r = Call POST "/api/auth/login" @{ email = $Email; motDePasse = $Password }
Check "login admin -> 200" ($r.Status -eq 200) "statut recu $($r.Status) : $($r.Raw)"
if ($r.Status -ne 200) { Write-Host "Connexion impossible : arret du script." -ForegroundColor Red; exit 1 }
Call GET "/api/auth/csrf" | Out-Null

$r = Call POST "/api/produits" @{ reference = "X-$run"; designation = "x"; prixUnitaireHT = 1; actif = $true; tauxTvaId = 1 } $false
Check "POST sans jeton CSRF -> 403" ($r.Status -eq 403) "statut recu $($r.Status)"

# ------------------------------------------------------------------ 1. TVA et produits

Titre "1. Taux de TVA et catalogue"
$r = Call GET "/api/taux-tva?actifsSeulement=true"
$tva20 = $r.Json | Where-Object { [decimal]$_.taux -eq 20 } | Select-Object -First 1
Check "au moins 5 taux de TVA actifs (V6)" (@($r.Json).Count -ge 5) "recu $(@($r.Json).Count)"
Check "un taux de 20 % existe" ($null -ne $tva20)

$prod = @{ reference = "TST-$run"; designation = "Prestation de test"; prixUnitaireHT = 100; marge = 10; actif = $true; tauxTvaId = $tva20.id }
$r = Call POST "/api/produits" $prod
Check "creation produit -> 201" ($r.Status -eq 201) "statut recu $($r.Status) : $($r.Raw)"
$produitId = $r.Json.id
Check "la reponse contient le taux de TVA du produit (20)" ((Dec $r.Json.tauxTva) -eq 20)

$r = Call POST "/api/produits" $prod
Check "reference deja utilisee -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"

$r = Call POST "/api/produits" @{ reference = ""; designation = "x"; prixUnitaireHT = 1; actif = $true; tauxTvaId = $tva20.id }
Check "reference vide -> 400 (validation)" ($r.Status -eq 400) "statut recu $($r.Status)"

$r = Call POST "/api/produits" @{ reference = "NEG-$run"; designation = "x"; prixUnitaireHT = -5; actif = $true; tauxTvaId = $tva20.id }
Check "prix negatif -> 400" ($r.Status -eq 400) "statut recu $($r.Status)"

$r = Call GET "/api/produits?page=0&size=1"
Check "pagination : size=1 renvoie 1 element" (@($r.Json.content).Count -eq 1) "recu $(@($r.Json.content).Count)"
Check "pagination : totalElements >= 1" ($r.Json.totalElements -ge 1)

$r = Call GET "/api/produits?q=TST-$run"
Check "recherche par reference -> 1 resultat" (@($r.Json.content).Count -eq 1)

$prodModifie = $prod.Clone()
$prodModifie["designation"] = "Prestation modifiee"
$r = Call PUT "/api/produits/$produitId" $prodModifie
Check "modification produit -> 200" ($r.Status -eq 200 -and $r.Json.designation -eq "Prestation modifiee")

$r = Call GET "/api/produits/999999"
Check "produit inexistant -> 404" ($r.Status -eq 404) "statut recu $($r.Status)"

# ------------------------------------------------------------------ 2. client (P2)

Titre "2. Client (endpoint de P2)"
$r = Call POST "/api/clients" @{ code = "CLI-$run"; raisonSociale = "Client Test $run"; type = "ENTREPRISE"; statut = "ACTIF"; delaiPaiementJours = 30 }
Check "creation client -> 201" ($r.Status -eq 201) "statut recu $($r.Status) : $($r.Raw)"
$clientId = $r.Json.id

# ------------------------------------------------------------------ 3. devis

Titre "3. Devis : creation et calcul des montants"
# ligne 1 : produit du catalogue (designation, prix et TVA repris) ; ligne 2 : ligne libre avec 10 % de remise
$devisCorps = @{ clientId = $clientId; lignes = @(
        @{ produitId = $produitId; quantite = 2 },
        @{ designation = "Formation"; quantite = 3; prixUnitaireHT = 50; remise = 10; tauxTVA = 20 }) }
$r = Call POST "/api/devis" $devisCorps
Check "creation devis -> 201" ($r.Status -eq 201) "statut recu $($r.Status) : $($r.Raw)"
$devis = $r.Json
Check "numero au format DEV-AAAA-NNNN" ($devis.numero -match '^DEV-\d{4}-\d{4}$') "numero $($devis.numero)"
Check "statut BROUILLON" ($devis.statut -eq "BROUILLON")
Check "ligne produit : designation reprise du catalogue" ($devis.lignes[0].designation -eq "Prestation de test")
Check "total HT = 200 + 135 = 335.00" ((Dec $devis.totalHT) -eq 335) "recu $($devis.totalHT)"
Check "total TVA = 40 + 27 = 67.00" ((Dec $devis.totalTVA) -eq 67) "recu $($devis.totalTVA)"
Check "total TTC = 402.00" ((Dec $devis.totalTTC) -eq 402) "recu $($devis.totalTTC)"

$r = Call POST "/api/devis" @{ clientId = $clientId; lignes = @() }
Check "devis sans ligne -> 400" ($r.Status -eq 400) "statut recu $($r.Status)"

$r = Call POST "/api/devis" @{ lignes = @(@{ designation = "x"; quantite = 1; prixUnitaireHT = 1; tauxTVA = 20 }) }
Check "devis sans client -> 400" ($r.Status -eq 400) "statut recu $($r.Status)"

$r = Call POST "/api/devis" @{ clientId = $clientId; lignes = @(@{ designation = "x"; quantite = 0; prixUnitaireHT = 1; tauxTVA = 20 }) }
Check "quantite = 0 -> 400" ($r.Status -eq 400) "statut recu $($r.Status)"

$r = Call POST "/api/devis" @{ clientId = $clientId; dateEmission = $today; dateValidite = (Get-Date).AddDays(-5).ToString("yyyy-MM-dd"); lignes = @(@{ designation = "x"; quantite = 1; prixUnitaireHT = 1; tauxTVA = 20 }) }
Check "validite avant emission -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"

Titre "4. Devis : cycle de vie"
$id = $devis.id
$r = Call POST "/api/devis/$id/acceptation"
Check "accepter un brouillon -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"

$r = Call PUT "/api/devis/$id" ($devisCorps + @{ dateValidite = (Get-Date).AddDays(30).ToString("yyyy-MM-dd") })
Check "modifier un brouillon -> 200" ($r.Status -eq 200) "statut recu $($r.Status) : $($r.Raw)"

$r = Call POST "/api/devis/$id/envoi"
Check "envoyer -> ENVOYE" ($r.Status -eq 200 -and $r.Json.statut -eq "ENVOYE")
$r = Call POST "/api/devis/$id/envoi"
Check "envoyer deux fois -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"
$r = Call PUT "/api/devis/$id" $devisCorps
Check "modifier un devis envoye -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"
$r = Call DELETE "/api/devis/$id"
Check "supprimer un devis envoye -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"

$r = Call POST "/api/devis/$id/acceptation"
Check "accepter -> ACCEPTE" ($r.Status -eq 200 -and $r.Json.statut -eq "ACCEPTE")

$r = Call POST "/api/devis" @{ clientId = $clientId; lignes = @(@{ designation = "A supprimer"; quantite = 1; prixUnitaireHT = 10; tauxTVA = 20 }) }
$brouillonId = $r.Json.id
$r = Call DELETE "/api/devis/$brouillonId"
Check "supprimer un brouillon -> 204" ($r.Status -eq 204) "statut recu $($r.Status)"
$r = Call GET "/api/devis/$brouillonId"
Check "devis supprime -> 404" ($r.Status -eq 404) "statut recu $($r.Status)"

$r = Call POST "/api/devis" @{ clientId = $clientId; lignes = @(@{ designation = "Refus"; quantite = 1; prixUnitaireHT = 10; tauxTVA = 20 }) }
$refusId = $r.Json.id
Call POST "/api/devis/$refusId/envoi" | Out-Null
$r = Call POST "/api/devis/$refusId/refus"
Check "refuser un devis envoye -> REFUSE" ($r.Status -eq 200 -and $r.Json.statut -eq "REFUSE")
$r = Call POST "/api/factures/depuis-devis/$refusId"
Check "facturer un devis refuse -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"

# ------------------------------------------------------------------ 5. facture

Titre "5. Facture : creation depuis le devis et emission"
$r = Call POST "/api/factures/depuis-devis/$id"
Check "creation facture depuis le devis accepte -> 201" ($r.Status -eq 201) "statut recu $($r.Status) : $($r.Raw)"
$facture = $r.Json
Check "statut BROUILLON et pas encore de numero" ($facture.statut -eq "BROUILLON" -and -not $facture.numero)
Check "les montants sont ceux du devis (402.00)" ((Dec $facture.totalTTC) -eq 402)
Check "les 2 lignes sont copiees" (@($facture.lignes).Count -eq 2)

$r = Call POST "/api/factures/depuis-devis/$id"
Check "facturer deux fois le meme devis -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"

$r = Call POST "/api/devis" @{ clientId = $clientId; lignes = @(@{ designation = "Brouillon"; quantite = 1; prixUnitaireHT = 10; tauxTVA = 20 }) }
$r = Call POST "/api/factures/depuis-devis/$($r.Json.id)"
Check "facturer un devis non accepte -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"

$r = Call POST "/api/factures/$($facture.id)/emission" @{ dateEmission = $today; dateEcheance = (Get-Date).AddDays(-1).ToString("yyyy-MM-dd") }
Check "echeance avant emission -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"

$r = Call POST "/api/factures/$($facture.id)/emission" @{}
Check "emission -> EMISE" ($r.Status -eq 200 -and $r.Json.statut -eq "EMISE") "statut recu $($r.Status) : $($r.Raw)"
Check "numero au format FAC-AAAA-NNNN" ($r.Json.numero -match '^FAC-\d{4}-\d{4}$') "numero $($r.Json.numero)"
$attendu = (Get-Date).AddDays(30).ToString("yyyy-MM-dd")
Check "echeance par defaut = emission + 30 jours ($attendu)" ($r.Json.dateEcheance -eq $attendu) "recu $($r.Json.dateEcheance)"
Check "reste a payer = total TTC" ((Dec $r.Json.resteAPayer) -eq 402)

$r = Call POST "/api/factures/$($facture.id)/emission" @{}
Check "emettre deux fois -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"

# ------------------------------------------------------------------ 6. paiements

Titre "6. Paiements"
$fid = $facture.id
$r = Call POST "/api/factures/$fid/paiements" @{ datePaiement = $today; montant = 1000; mode = "VIREMENT" }
Check "paiement superieur au reste -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"
$r = Call POST "/api/factures/$fid/paiements" @{ datePaiement = $today; montant = -5; mode = "VIREMENT" }
Check "paiement negatif -> 400" ($r.Status -eq 400) "statut recu $($r.Status)"
$r = Call POST "/api/factures/$fid/paiements" @{ datePaiement = $today; montant = 10; mode = "BITCOIN" }
Check "mode de paiement inconnu -> 400" ($r.Status -eq 400) "statut recu $($r.Status)"

$r = Call POST "/api/factures/$fid/paiements" @{ datePaiement = $today; montant = 100; mode = "CHEQUE"; reference = "CHQ-$run" }
Check "paiement partiel de 100 -> 201" ($r.Status -eq 201) "statut recu $($r.Status) : $($r.Raw)"
Check "statut PARTIELLEMENT_PAYEE" ($r.Json.statut -eq "PARTIELLEMENT_PAYEE")
Check "reste a payer = 302.00" ((Dec $r.Json.resteAPayer) -eq 302) "recu $($r.Json.resteAPayer)"

$r = Call POST "/api/factures/$fid/annulation"
Check "annuler une facture deja payee en partie -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"

$r = Call POST "/api/factures/$fid/paiements" @{ datePaiement = $today; montant = 302; mode = "VIREMENT" }
Check "solde du reste -> PAYEE" ($r.Status -eq 201 -and $r.Json.statut -eq "PAYEE")
Check "reste a payer = 0" ((Dec $r.Json.resteAPayer) -eq 0)

$r = Call POST "/api/factures/$fid/paiements" @{ datePaiement = $today; montant = 1; mode = "VIREMENT" }
Check "payer une facture deja soldee -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"

$r = Call GET "/api/factures/$fid/paiements"
Check "la liste des paiements en contient 2" (@($r.Json).Count -eq 2) "recu $(@($r.Json).Count)"

$r = Call POST "/api/factures/999999/paiements" @{ datePaiement = $today; montant = 1; mode = "VIREMENT" }
Check "paiement sur une facture inexistante -> 404" ($r.Status -eq 404) "statut recu $($r.Status)"

# ------------------------------------------------------------------ 7. annulation

Titre "7. Annulation"
$f3 = Nouvelle-Facture $clientId 80 $null $null
$r = Call POST "/api/factures/$($f3.id)/annulation"
Check "annuler une facture emise sans paiement -> ANNULEE" ($r.Status -eq 200 -and $r.Json.statut -eq "ANNULEE")
$r = Call POST "/api/factures/$($f3.id)/annulation"
Check "annuler deux fois -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"
$r = Call POST "/api/factures/$($f3.id)/paiements" @{ datePaiement = $today; montant = 10; mode = "VIREMENT" }
Check "payer une facture annulee -> 422" ($r.Status -eq 422) "statut recu $($r.Status)"

# ------------------------------------------------------------------ 8. retard, relances, balance agee

Titre "8. Facture en retard, relances de paiement et balance agee"
$emission = (Get-Date).AddDays(-60).ToString("yyyy-MM-dd")
$echeance = (Get-Date).AddDays(-20).ToString("yyyy-MM-dd")
$f2 = Nouvelle-Facture $clientId 335 $emission $echeance
Check "facture emise avec echeance passee (il y a 20 jours)" ($f2.statut -eq "EMISE" -and $f2.dateEcheance -eq $echeance)

$r = Call POST "/api/relances/paiement/generer?date=$today"
Check "traitement de nuit -> 200" ($r.Status -eq 200) "statut recu $($r.Status) : $($r.Raw)"
Check "au moins 1 facture passee en retard" ($r.Json.facturesPasseesEnRetard -ge 1) "recu $($r.Json.facturesPasseesEnRetard)"
Check "au moins 1 relance creee" ($r.Json.relancesCreees -ge 1) "recu $($r.Json.relancesCreees)"

$r = Call GET "/api/factures/$($f2.id)"
Check "la facture est EN_RETARD" ($r.Json.statut -eq "EN_RETARD") "statut $($r.Json.statut)"

$r = Call GET "/api/relances?size=100"
$mienne = @($r.Json.content) | Where-Object { $_.clientId -eq $clientId -and $_.nature -eq "PAIEMENT" }
Check "une relance PAIEMENT existe pour ce client" (@($mienne).Count -ge 1) "liste vide (statut $($r.Status))"
$niveau3 = @($mienne) | Where-Object { $_.motif -like "*J+15*" }
Check "elle est de niveau 3 (J+15) : 20 jours apres l'echeance" (@($niveau3).Count -eq 1) "motifs : $((@($mienne) | ForEach-Object { $_.motif }) -join ' | ')"
Check "statut initial A_FAIRE" (@($niveau3)[0].statut -eq "A_FAIRE")

$r = Call POST "/api/relances/paiement/generer?date=$today"
Check "2e execution le meme jour : 0 relance creee (pas de doublon)" ($r.Json.relancesCreees -eq 0) "recu $($r.Json.relancesCreees)"
Check "2e execution le meme jour : 0 facture passee en retard" ($r.Json.facturesPasseesEnRetard -eq 0)

# suivi par P2 : demarrer puis traiter
if (@($niveau3).Count -ge 1) {
    $rid = @($niveau3)[0].id
    $r = Call POST "/api/relances/$rid/demarrer"
    Check "demarrer la relance -> EN_COURS" ($r.Status -eq 200 -and $r.Json.statut -eq "EN_COURS") "statut recu $($r.Status)"
    $r = Call POST "/api/relances/$rid/traiter" @{ resultat = "SANS_REPONSE"; commentaire = "Test automatique" }
    Check "traiter la relance -> TERMINEE" ($r.Status -eq 200 -and $r.Json.statut -eq "TERMINEE") "statut recu $($r.Status) : $($r.Raw)"
    $r = Call POST "/api/relances/paiement/generer?date=$today"
    Check "relance terminee : toujours aucune nouvelle relance au meme niveau" ($r.Json.relancesCreees -eq 0)
}

$r = Call GET "/api/factures/balance-agee?date=$today"
$ligne = @($r.Json) | Where-Object { $_.clientId -eq $clientId }
Check "la balance agee contient ce client" ($null -ne $ligne)
Check "335 HT + 20 % = 402.00 en tranche 1-30 jours" ((Dec $ligne.retard1a30) -eq 402) "recu $($ligne.retard1a30)"
Check "la facture payee et la facture annulee ne comptent pas" ((Dec $ligne.nonEchu) -eq 0 -and (Dec $ligne.retard31a60) -eq 0)

Titre "9. Voyage dans le temps : +30 jours"
$futur = (Get-Date).AddDays(30).ToString("yyyy-MM-dd")
$r = Call POST "/api/relances/paiement/generer?date=$futur"
Check "50 jours de retard : nouvelle relance de niveau 5 (J+45)" ($r.Json.relancesCreees -ge 1) "recu $($r.Json.relancesCreees)"
$r = Call GET "/api/factures/balance-agee?date=$futur"
$ligne = @($r.Json) | Where-Object { $_.clientId -eq $clientId }
Check "la meme creance passe en tranche 31-60 jours" ((Dec $ligne.retard31a60) -eq 402) "recu $($ligne.retard31a60)"

# ------------------------------------------------------------------ 10. erreurs generales

Titre "10. Erreurs generales"
$r = Call GET "/api/factures/999999"
Check "facture inexistante -> 404" ($r.Status -eq 404) "statut recu $($r.Status)"
$r = Call GET "/api/factures?statut=N_IMPORTE_QUOI"
Check "statut de filtre inconnu -> 400" ($r.Status -eq 400) "statut recu $($r.Status)"
$r = Call GET "/api/factures?statut=EN_RETARD&size=5"
Check "filtre par statut EN_RETARD -> 200 avec resultats" ($r.Status -eq 200 -and @($r.Json.content).Count -ge 1)
$r = Call GET "/api/factures?size=2&page=0"
Check "liste paginee des factures (size=2)" (@($r.Json.content).Count -le 2 -and $r.Json.totalElements -ge 3)
$r = Call DELETE "/api/produits/$produitId"
Check "desactivation du produit -> actif = false" ($r.Status -eq 200 -and $r.Json.actif -eq $false)

# ------------------------------------------------------------------ 11. concurrence

Titre "11. Concurrence : deux paiements simultanes (verrou pessimiste)"
$f4 = Nouvelle-Facture $clientId 100 $null $null          # TTC 120.00
$cookie = "JSESSIONID=$(Get-Cookie 'JSESSIONID'); XSRF-TOKEN=$(Get-Cookie 'XSRF-TOKEN')"
$token = Get-Cookie "XSRF-TOKEN"
$corps = (@{ datePaiement = $today; montant = 120; mode = "VIREMENT" } | ConvertTo-Json)
$travail = {
    param($url, $cookie, $token, $corps)
    try {
        $r = Invoke-WebRequest -Uri $url -Method POST -Body $corps -ContentType "application/json" -UseBasicParsing `
            -Headers @{ "Cookie" = $cookie; "X-XSRF-TOKEN" = $token; "Accept" = "application/json" }
        return [int]$r.StatusCode
    } catch { return [int]$_.Exception.Response.StatusCode }
}
$j1 = Start-Job -ScriptBlock $travail -ArgumentList "$Base/api/factures/$($f4.id)/paiements", $cookie, $token, $corps
$j2 = Start-Job -ScriptBlock $travail -ArgumentList "$Base/api/factures/$($f4.id)/paiements", $cookie, $token, $corps
$codes = @(Receive-Job -Job $j1, $j2 -Wait -AutoRemoveJob) | Sort-Object
Check "un seul des deux paiements de 120 est accepte (201 + 422)" (($codes -join ",") -eq "201,422") "codes recus : $($codes -join ',')"
$r = Call GET "/api/factures/$($f4.id)"
Check "la facture est PAYEE, reste 0, un seul paiement" ($r.Json.statut -eq "PAYEE" -and (Dec $r.Json.resteAPayer) -eq 0 -and @($r.Json.paiements).Count -eq 1)

# ------------------------------------------------------------------ fin

Titre "12. Deconnexion"
$r = Call POST "/api/auth/logout"
Check "logout -> 204" ($r.Status -eq 204) "statut recu $($r.Status)"
$r = Call GET "/api/produits"
Check "apres la deconnexion -> 401" ($r.Status -eq 401) "statut recu $($r.Status)"

Write-Host ""
$couleur = "Green"
if ($script:ko -gt 0) { $couleur = "Red" }
Write-Host "BILAN : $($script:ok) verifications reussies, $($script:ko) echecs" -ForegroundColor $couleur
if ($script:ko -gt 0) { exit 1 }
