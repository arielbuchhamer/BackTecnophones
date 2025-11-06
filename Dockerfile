# Etapa 1: compilar el proyecto
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiamos el pom y descargamos dependencias
COPY pom.xml .
RUN ./mvnw dependency:go-offline || true

# Copiamos el resto del código y construimos el jar
COPY . .
RUN ./mvnw clean package -DskipTests

# Etapa 2: imagen final (más liviana)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copiamos el jar compilado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Puerto por defecto
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]
