# Dockerfile para aplicação Spring Boot
FROM eclipse-temurin:17-jdk-alpine as build

# Instalar dependências necessárias
RUN apk add --no-cache curl

WORKDIR /app

# Copiar apenas arquivos de dependências primeiro (para cache otimizado)
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./

# Tornar mvnw executável
RUN chmod +x ./mvnw

# Baixar dependências (esta camada será cacheada se pom.xml não mudar)
RUN ./mvnw dependency:go-offline -B --no-transfer-progress

# Copiar código fonte
COPY src ./src

# Compilar aplicação
RUN ./mvnw clean package -DskipTests --no-transfer-progress

FROM eclipse-temurin:17-jre-alpine

# Instalar curl para health check
RUN apk add --no-cache curl

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 3001
ENTRYPOINT ["java", "-Xms256m", "-Xmx1024m", "-jar", "app.jar"]
