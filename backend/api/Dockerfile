# ---- build ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Optional version metadata (no git in this image)
ARG LOCUS_COMMIT=
ARG LOCUS_TAG=
ARG LOCUS_UPDATE_URL=
ENV LOCUS_COMMIT=$LOCUS_COMMIT \
    LOCUS_TAG=$LOCUS_TAG \
    LOCUS_UPDATE_URL=$LOCUS_UPDATE_URL

COPY gradlew settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY buildSrc ./buildSrc
COPY backend ./backend
COPY extensions ./extensions
COPY config ./config

RUN chmod +x gradlew
# Daemon, CLI, and thin extension jars (SPI-loaded at runtime)
RUN ./gradlew \
    :backend:api:installDist \
    :backend:cli:installDist \
    :extensions:links:jar \
    --no-daemon -x test

# ---- runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S locus && adduser -S locus -G locus

# Daemon and CLI in separate trees so one COPY does not clobber the other
COPY --from=build /workspace/backend/api/build/install/locus /app
COPY --from=build /workspace/backend/cli/build/install/locusctl /cli

# Bundled extension jars (host can add more under extensions/drop-in)
RUN mkdir -p /app/config /app/extensions/drop-in
COPY --from=build /workspace/extensions/links/build/libs/locus-extension-links*.jar /app/extensions/
RUN chown -R locus:locus /app /cli

ENV PATH="/app/bin:/cli/bin:${PATH}"

USER locus

EXPOSE 8080

WORKDIR "/app"
# locus <configdirectory> api [<options>]
ENTRYPOINT ["/app/bin/locus"]
CMD ["/app/config", "api", "--host", "0.0.0.0", "--port", "8080"]
