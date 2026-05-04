# Lightweight runtime-only image — JAR is built locally
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN mkdir -p /app/data

# Copy the pre-built JAR (build locally with: mvn package -o -DskipTests)
COPY target/homework-alert-1.0.0.jar app.jar

ENV SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/homework-alert
ENV SERVER_PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
