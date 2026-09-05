FROM eclipse-temurin:25-jre

WORKDIR /app

ARG JAR_FILE=build/libs/stocktracker-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
