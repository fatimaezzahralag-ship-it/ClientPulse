# =============================================================================
# Backend ClientPulse : image en deux étapes (build Maven, puis JRE seul).
# Construction : docker build -t clientpulse-backend .
# =============================================================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Les dépendances sont téléchargées dans une couche à part : elle n'est rejouée que si le pom change.
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -DskipTests package \
 && cp target/*.jar /build/app.jar

FROM eclipse-temurin:17-jre
RUN groupadd --system app && useradd --system --gid app --no-create-home app
WORKDIR /app
COPY --from=build /build/app.jar app.jar
USER app

EXPOSE 8080
# Le fuseau « Africa/Casablanca » est celui des tâches planifiées de nuit.
ENV TZ=Africa/Casablanca \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
