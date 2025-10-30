# Stage 1: Build
FROM maven:3.9.2-eclipse-temurin-20 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:20-jre-alpine
WORKDIR /app
COPY --from=build /app/target/dynamaticball-0.0.1-SNAPSHOT.jar app.jar

# Optional: set profile to docker (so no DB connection at build)
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java","-jar","app.jar"]
