FROM openjdk:11-jdk-slim
WORKDIR /app
COPY deployment/application.jar /app/application.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/application.jar"]