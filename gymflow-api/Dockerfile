# ---- Etapa 1: compilar el proyecto ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
# Si mvnw se guardó con saltos de línea de Windows, en Linux no corre: se corrige aquí
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

COPY src src
RUN ./mvnw -B -DskipTests package

# ---- Etapa 2: imagen final, solo con Java y el .jar ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Memoria: Render gratis tiene 512 MB, así que se limita el uso de la JVM
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC"

EXPOSE 8080
# Render indica el puerto en la variable PORT
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]