FROM openjdk:11-jre-slim
WORKDIR /app
COPY build/libs/*.jar application.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/application.jar"]