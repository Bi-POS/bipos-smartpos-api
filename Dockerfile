# =========================
# BUILD STAGE
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Código fonte
COPY src ./src

# Build do JAR
RUN mvn clean package -DskipTests


# =========================
# RUNTIME STAGE
# =========================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copia o jar gerado
COPY --from=build /app/target/*.jar app.jar

# Porta padrão (cloud / local)
EXPOSE 8080

# Ambiente
ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=America/Sao_Paulo

# JVM enxuta para POS / Android
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=60","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=prod","-jar","app.jar"]
