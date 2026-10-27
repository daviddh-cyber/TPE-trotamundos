# Se construye desde la raíz del repositorio (ver docker-compose.yml),
# porque el build necesita la carpeta specs/ para generar las interfaces.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY specs ./specs
COPY servicios/viajes ./servicios/viajes
WORKDIR /app/servicios/viajes
RUN mvn -B package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/servicios/viajes/target/viajes.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
