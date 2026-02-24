FROM openjdk:11-jdk AS build-stage

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew bootJar

FROM openjdk:17-jdk
COPY --from=build-stage build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar","-Dspring.profiles.active=gcp", "/app.jar"]
EXPOSE 8080
