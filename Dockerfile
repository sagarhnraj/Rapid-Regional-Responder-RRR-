# Multi-stage Docker build for Java Spring Boot on Render
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

ENV MAVEN_OPTS="-Xmx384m -XX:+UseG1GC"

COPY . .
RUN if [ -f "pom.xml" ]; then mvn clean package -DskipTests -B && mkdir -p /app/build_output && cp target/*.jar /app/build_output/; else cd backend && mvn clean package -DskipTests -B && mkdir -p /app/build_output && cp target/*.jar /app/build_output/; fi

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build_output/*.jar app.jar
EXPOSE 8080

ENV JAVA_OPTS="-Xmx384m -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
