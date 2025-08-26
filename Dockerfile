# Multi-stage build para Quarkus
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Definir diretório de trabalho
WORKDIR /code

# Copiar arquivos de configuração Maven primeiro (para cache)
COPY pom.xml /code/
COPY .mvn /code/.mvn
COPY mvnw /code/
COPY mvnw.cmd /code/

# Dar permissões ao wrapper do Maven
RUN chmod +x mvnw

# Baixar dependências (layer de cache)
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src /code/src

# Build da aplicação com configurações otimizadas
RUN ./mvnw clean package -DskipTests -B \
    -Dquarkus.package.type=uber-jar \
    -Dmaven.repo.local=/tmp/.m2

# Stage de runtime
FROM eclipse-temurin:17-jre-alpine

# Instalar dependências necessárias
RUN apk add --no-cache curl tzdata

# Configurar timezone
ENV TZ=America/Sao_Paulo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Criar usuário não-root
RUN addgroup -g 1001 -S quarkus && \
    adduser -u 1001 -S quarkus -G quarkus

# Criar diretórios necessários
RUN mkdir -p /app/logs && \
    chown -R quarkus:quarkus /app

# Definir diretório de trabalho
WORKDIR /app

# Copiar JAR da aplicação
COPY --from=build --chown=quarkus:quarkus /code/target/*-runner.jar /app/app.jar

# Mudar para usuário não-root
USER quarkus

# Expor porta
EXPOSE 8080

# Configurações de JVM otimizadas para container
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Configurações específicas do Quarkus
ENV QUARKUS_HTTP_HOST=0.0.0.0 \
    QUARKUS_HTTP_PORT=8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/q/health/ready || exit 1

# Comando para executar a aplicação
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]