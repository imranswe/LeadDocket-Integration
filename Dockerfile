FROM maven:3.9.12-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src src
RUN mvn -B clean verify
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /app/data && chown -R 10001:10001 /app
COPY --from=build /build/target/leaddocket-integration-1.0.0.jar app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
