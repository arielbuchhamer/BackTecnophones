# ----- build -----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
# Cachea deps en builds repetidos (requiere BuildKit)
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -DskipTests dependency:go-offline

COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -DskipTests clean package

# ----- runtime -----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario no-root
RUN useradd -r -u 10001 appuser
USER appuser

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080

# Ajustes razonables para 1vCPU/2GB
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:InitialRAMPercentage=30 -Djava.security.egd=file:/dev/./urandom"

CMD ["java", "-jar", "/app/app.jar"]