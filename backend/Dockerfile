FROM eclipse-temurin:17-jdk-slim AS build-stage
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew bootJar

FROM eclipse-temurin:17-jre-slim
WORKDIR /app

COPY --from=build-stage /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar","-Dspring.profiles.active=gcp","app.jar"]
EXPOSE 8080
