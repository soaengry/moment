FROM eclipse-temurin:17-jdk-alpine AS build-stage
ARG JAR_FILE=backend/build/libs/*.jar
ARG PROFILES
ARG ENV
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${PROFILES}", "-Dserver.env=${ENV}", "app.jar"]
