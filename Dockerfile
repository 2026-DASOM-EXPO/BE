FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --create-home --uid 10001 worksafe

WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

RUN mkdir -p /app/build/hls \
    && chown -R worksafe:worksafe /app

USER worksafe

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=45s --retries=5 \
    CMD curl --fail --silent http://localhost:8080/v3/api-docs > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
