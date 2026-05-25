# ---- Stage 1: Build ----
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copia wrapper e pom primeiro para cache de dependências
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B --no-transfer-progress

# Compila
COPY src ./src
RUN ./mvnw clean package -DskipTests --no-transfer-progress

# Extrai layers do JAR para cache granular no Docker
RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Timezone + usuário não-root em um único layer
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/America/Sao_Paulo /etc/localtime && \
    echo "America/Sao_Paulo" > /etc/timezone && \
    apk del tzdata && \
    addgroup -S appgroup && adduser -S appuser -G appgroup

# Copia layers extraídas em ordem (dependências mudam menos, app muda mais)
# Isso maximiza reuso de cache entre deploys
COPY --from=builder --chown=appuser:appgroup /app/target/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/target/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /app/target/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/target/extracted/application/ ./

USER appuser

CMD ["java", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-XX:InitialRAMPercentage=50.0", \
     "-XX:+UseG1GC", \
     "-XX:+ExitOnOutOfMemoryError", \
     "-Duser.timezone=America/Sao_Paulo", \
     "-Dfile.encoding=UTF-8", \
     "org.springframework.boot.loader.launch.JarLauncher"]
