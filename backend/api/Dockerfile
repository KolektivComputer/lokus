# ---- build ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY buildSrc ./buildSrc
COPY backend ./backend
COPY extensions ./extensions

RUN chmod +x gradlew

# Daemon + CLI distributions
RUN ./gradlew :backend:api:installDist :backend:cli:installDist --no-daemon -x test

# ---- runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S locus && adduser -S locus -G locus

COPY --from=build /workspace/backend/api/build/install/locus /app
COPY --from=build /workspace/backend/cli/build/install/locus-cli /cli

RUN mkdir -p /app/config /app/extensions \
    && chown -R locus:locus /app /cli

USER locus

EXPOSE 8080

# PATH includes locus-cli for exec from the same image
ENV PATH="/app/bin:/cli/bin:${PATH}"

# Usage: locus api [<options>] <configdirectory>
# CLI:   locus-cli version
ENTRYPOINT ["/app/bin/locus"]
CMD ["/app/config", "api", "--host", "0.0.0.0", "--port", "8080"]
