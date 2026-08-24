# Multi-stage Docker build for Java Spring Boot on Render
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

ENV MAVEN_OPTS="-Xmx256m -Dmaven.wagon.http.retryHandler.count=3"

COPY pom.xml .
RUN mvn dependency:go-offline -B || true

COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/app.jar app.jar
EXPOSE 8080

ENV JAVA_OPTS="-Xmx256m"
ENTRYPOINT ["java", "-jar", "app.jar"]
