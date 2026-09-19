# ETAPA 1: Construcción (Builder)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copiar el pom.xml primero para aprovechar el caché de capas de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar saltando los tests para mayor velocidad
COPY src ./src
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Runtime)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar solo el .jar generado desde la etapa de construcción
COPY --from=builder /app/target/*.jar app.jar

# El catálogo usa el puerto 8082 (según tu application.yml)
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]