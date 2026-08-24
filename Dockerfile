# Multi-stage Docker build for Java Spring Boot on Render (Memory Optimized)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

ENV MAVEN_OPTS="-Xmx384m -XX:+UseG1GC"

COPY backend/pom.xml .
COPY backend/src ./src

RUN mvn clean package -DskipTests -B && rm -f target/*.original

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/regional-responder-backend-*.jar app.jar
EXPOSE 8080

ENV JAVA_OPTS="-Xmx384m -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
