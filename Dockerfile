# Stage 1: build the Spring Boot JAR with Maven (discarded after this stage).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

# Stage 2: run only the JAR on a small Java runtime image.
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/nyc-transit-tracker-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
