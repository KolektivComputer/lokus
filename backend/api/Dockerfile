# ---- build ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Gradle wrapper + project metadata first (better layer cache)
COPY gradlew settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY backend/api/build.gradle.kts ./backend/api/
COPY config ./config

RUN chmod +x gradlew

# Dependencies (no sources yet)
RUN ./gradlew :backend:api:dependencies --no-daemon || true

# Sources + build installDist
COPY backend/api/src ./backend/api/src
RUN ./gradlew :backend:api:installDist --no-daemon -x test

# ---- runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S locus && adduser -S locus -G locus

COPY --from=build /workspace/backend/api/build/install/locus /app

# Config + extension jars mounted at runtime
RUN mkdir -p /app/config /app/extensions && chown -R locus:locus /app

USER locus

EXPOSE 8080

# Usage: locus api [<options>] <configdirectory>
WORKDIR "/app"
ENTRYPOINT ["/app/bin/locus"]
CMD ["/app/config", "api", "--host", "0.0.0.0", "--port", "8080"]
