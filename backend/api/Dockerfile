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
COPY backend/api/build.gradle.kts ./backend/api/
COPY config ./config

RUN chmod +x gradlew

RUN ./gradlew :backend:api:dependencies --no-daemon || true

COPY backend/api/src ./backend/api/src
RUN ./gradlew :backend:api:installDist --no-daemon -x test

# ---- runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S locus && adduser -S locus -G locus

COPY --from=build /workspace/backend/api/build/install/locus /app

RUN mkdir -p /app/config /app/extensions && chown -R locus:locus /app

USER locus

EXPOSE 8080

WORKDIR "/app"
ENTRYPOINT ["/app/bin/locus"]
CMD [ "/app/config", "api", "--host", "0.0.0.0", "--port", "8080"]
