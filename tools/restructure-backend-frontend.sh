#!/usr/bin/env bash
# =============================================================================
# Passe le dépôt à la structure backend/ + frontend/ (décision A12 du plan).
#
# À lancer UNE FOIS, à la racine du dépôt, sur une branche propre, quand P1 et P2 ont tout fusionné dans
# develop (sinon leurs branches auront des conflits de renommage). Ensuite : git pull --rebase pour tous.
#
#     bash tools/restructure-backend-frontend.sh
# =============================================================================
set -euo pipefail

if ! git diff --quiet || ! git diff --cached --quiet; then
  echo "Des modifications ne sont pas commitées : commitez ou stashez d'abord." >&2
  exit 1
fi
[ -f pom.xml ] && [ -d clientpulse-front ] || { echo "À lancer à la racine du dépôt (pom.xml et clientpulse-front/ attendus)." >&2; exit 1; }

mkdir -p backend
for chemin in src pom.xml mvnw mvnw.cmd .mvn Dockerfile .dockerignore; do
  [ -e "$chemin" ] && git mv "$chemin" "backend/$chemin"
done
git mv clientpulse-front frontend

# Références aux anciens chemins
sed -i 's#build: \.$#build: ./backend#; s#\./clientpulse-front#./frontend#g' docker-compose.yml
sed -i 's#clientpulse-front#frontend#g' .github/workflows/ci.yml
python3 - <<'PY'
import re, pathlib
ci = pathlib.Path(".github/workflows/ci.yml")
t = ci.read_text(encoding="utf-8")
# le job backend s'exécute dans backend/
t = t.replace("    name: Backend (Java 17, Maven)\n    runs-on: ubuntu-latest\n",
              "    name: Backend (Java 17, Maven)\n    runs-on: ubuntu-latest\n    defaults:\n      run:\n        working-directory: backend\n", 1)
t = t.replace("cache: maven\n", "cache: maven\n          cache-dependency-path: backend/pom.xml\n", 1)
t = t.replace("path: target/surefire-reports", "path: backend/target/surefire-reports")
ci.write_text(t, encoding="utf-8")

rd = pathlib.Path("README.md")
r = rd.read_text(encoding="utf-8")
r = r.replace("clientpulse-front", "frontend")
r = r.replace("├── src/main/java/ma/ensam/clientpulse/    Backend Spring Boot", "├── backend/               Backend Spring Boot (src/, pom.xml, Dockerfile)")
r = r.replace("./mvnw clean verify        # unitaires", "(cd backend && ./mvnw clean verify)   # unitaires")
r = r.replace("./mvnw spring-boot:run -Dspring-boot.run.profiles=dev", "cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev")
rd.write_text(r, encoding="utf-8")
PY
sed -i 's#clientpulse-front#frontend#g' docs/*.md tools/generate_demo_data.py 2>/dev/null || true
sed -i 's#"src/main/resources/db/dev#"backend/src/main/resources/db/dev#' tools/generate_demo_data.py
sed -i 's#parent.parent / "src/main/resources#parent.parent / "backend/src/main/resources#' tools/generate_demo_data.py

echo "Structure mise à jour. Vérifiez : git status, puis (cd backend && ./mvnw verify) et (cd frontend && npm ci && npm test)."
