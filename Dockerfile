# Multi-stage Docker build for Java Spring Boot on Render
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

ENV MAVEN_OPTS="-Xmx384m -DskipTests"

COPY backend/pom.xml ./pom.xml
COPY backend/src ./src

RUN mvn package -DskipTests -Dmaven.test.skip=true -Djar.finalName=app -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/app.jar app.jar
EXPOSE 8080

ENV JAVA_OPTS="-Xmx384m"
ENTRYPOINT ["java", "-jar", "app.jar"]
