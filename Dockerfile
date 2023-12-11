FROM openjdk:11-jre-slim

COPY target/validation-server-0.0.1-SNAPSHOT.jar validation-server-0.0.1-SNAPSHOT.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/validation-server-0.0.1-SNAPSHOT.jar"]