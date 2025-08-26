# Etapa de build
FROM maven:3.9.4-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -Dquarkus.package.type=uber-jar

# Etapa de runtime
FROM eclipse-temurin:17-jdk-alpine

# Instala o tzdata e define o timezone
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/America/Sao_Paulo /etc/localtime \
    && echo "America/Sao_Paulo" > /etc/timezone \
    && apk del tzdata

WORKDIR /app
COPY --from=build /app/target/*-runner.jar app.jar
EXPOSE 8080

# Configurações de JVM otimizadas para produção + perfil prod
ENTRYPOINT ["java", \
    "-Xms512m", \
    "-Xmx2g", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-XX:+UseStringDeduplication", \
    "-Duser.timezone=America/Sao_Paulo", \
    "-jar", "app.jar"]