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
RUN ./gradlew :backend:api:installDist --no-daemon -x test -x detekt

# ---- runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S thingy && adduser -S thingy -G thingy

COPY --from=build /workspace/backend/api/build/install/api /app

# Config + extension jars mounted at runtime
RUN mkdir -p /config /extensions && chown -R thingy:thingy /app /config /extensions

USER thingy

EXPOSE 8080

# PlatformCliktCommand requires config dir as first arg, then subcommand.
# Default entry runs the API; override via compose/command as needed.
ENTRYPOINT ["/app/bin/api"]
CMD ["/config", "api", "--host", "0.0.0.0", "--port", "8080"]
