# Se construye desde la raíz del repositorio (ver docker-compose.yml),
# porque el build necesita la carpeta specs/ para generar las interfaces.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY specs ./specs
COPY servicios/proveedores ./servicios/proveedores
WORKDIR /app/servicios/proveedores
RUN mvn -B package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/servicios/proveedores/target/proveedores.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
