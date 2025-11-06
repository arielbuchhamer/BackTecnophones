FROM maven:3.9-eclipse-temurin-21
WORKDIR /app

# Bajar dependencias primero (cachea mejor)
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

# Copiar código y compilar
COPY . .
RUN mvn -B -ntp clean package -DskipTests

# Exponer puerto
EXPOSE 9000

# Ejecutar el jar generado
CMD ["sh", "-c", "java -jar target/*.jar"]
