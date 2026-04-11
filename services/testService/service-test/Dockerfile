# Build
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Runtime (JAR name = artifactId-version from pom.xml)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /build/target/service-test-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
