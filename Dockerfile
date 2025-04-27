# Stage 1: Build
FROM gradle:8-jdk-21-and-22-alpine AS builder
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradlew gradlew.bat ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew && ./gradlew clean shadowJar -x test

# Stage 2: Runtime image
FROM eclipse-temurin:23-alpine
WORKDIR /app

# shadowJar now produces build/libs/discord-bot-1.0-SNAPSHOT.jar (no -all)
COPY --from=builder /app/build/libs/*.jar discord.jar

# Start
ENTRYPOINT ["java", "-jar", "discord.jar"]
