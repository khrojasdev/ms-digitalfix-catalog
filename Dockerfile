# =============================================================================
# ms-digitalfix-catalog
# =============================================================================
# Multi-stage: la primera etapa compila con Maven, la segunda solo lleva el jar
# y un JRE. La imagen final no contiene Maven, ni el codigo fuente, ni el
# repositorio local de dependencias: pesa una fraccion y expone menos.
# =============================================================================

# ------------------------------------------------------------------ compilar
FROM maven:3.9-eclipse-temurin-17 AS construccion

WORKDIR /construccion

# El pom va solo primero: mientras no cambien las dependencias, Docker reutiliza
# esta capa y no vuelve a bajar medio Maven Central en cada build.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ------------------------------------------------------------------ ejecutar
FROM eclipse-temurin:17-jre-alpine

# Nunca como root: si alguien se escapa del proceso, se escapa a un usuario que
# no puede hacer nada.
RUN addgroup -S digitalfix && adduser -S digitalfix -G digitalfix

WORKDIR /app
COPY --from=construccion /construccion/target/*.jar app.jar
RUN chown -R digitalfix:digitalfix /app

USER digitalfix

EXPOSE 8082

# El healthcheck usa el endpoint de actuator, no un simple "el puerto responde":
# el puerto puede estar arriba con la base de datos caida.
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -q -O - http://localhost:8082/actuator/health | grep -q '"status":"UP"' || exit 1

# Container-aware: la JVM respeta el limite de memoria del contenedor en vez de
# mirar la RAM de la maquina entera.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
