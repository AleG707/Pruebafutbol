# ==========================================
# Etapa 1: Compilación con Maven y Java 17
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar configuración de dependencias y código fuente
COPY pom.xml .
COPY src ./src

# Compilar empaquetando el JAR sin ejecutar pruebas
RUN mvn clean package -DskipTests

# ==========================================
# Etapa 2: Imagen final ligera de ejecución (JRE 17)
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el archivo JAR generado desde la etapa de compilación
COPY --from=build /app/target/CRUD-clubes-apirest-1.jar app.jar

# Puerto por defecto
ENV PORT=8888
EXPOSE 8888

# Ejecutar la aplicación Spring Boot vinculando el puerto dinámico de Render
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]