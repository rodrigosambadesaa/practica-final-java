FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B clean package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S garaje && adduser -S garaje -G garaje
WORKDIR /app

COPY --from=build /workspace/target/garaje-api-*.jar app.jar
RUN mkdir -p /app/data && chown -R garaje:garaje /app

USER garaje
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
