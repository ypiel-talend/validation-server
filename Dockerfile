FROM openjdk:21-jdk

COPY target/validation-server-0.0.2-SNAPSHOT.jar validation-server-0.0.2-SNAPSHOT.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/validation-server-0.0.2-SNAPSHOT.jar"]