# Dockerfile para aplicação Spring Boot
FROM eclipse-temurin:17-jdk-alpine as build

WORKDIR /app

# Copiar apenas arquivos de dependências primeiro
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Baixar dependências (esta camada será cacheada)
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Compilar aplicação
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 3001
ENTRYPOINT ["java", "-Xms256m", "-Xmx1024m", "-jar", "app.jar"]
