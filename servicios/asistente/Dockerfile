# Se construye desde la raíz del repositorio (ver docker-compose.yml),
# porque el build necesita la carpeta specs/ para generar las interfaces.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY specs ./specs
COPY servicios/asistente ./servicios/asistente
WORKDIR /app/servicios/asistente
RUN mvn -B package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/servicios/asistente/target/asistente.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
