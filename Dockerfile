FROM eclipse-temurin:17-jre

WORKDIR /app

# Copiamos el JAR generado
COPY target/*.jar app.jar

# Puerto de la aplicación
EXPOSE 8080

# Arranque
ENTRYPOINT ["java", "-jar", "/app/app.jar"]