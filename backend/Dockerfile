# Multi-stage Docker build for Java Spring Boot on Render
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

ENV MAVEN_OPTS="-Xmx384m -XX:+UseG1GC"

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/app.jar app.jar
EXPOSE 8080

ENV JAVA_OPTS="-Xmx384m -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
