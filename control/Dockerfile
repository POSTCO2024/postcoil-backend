FROM openjdk:11-jre-slim as builder
WORKDIR /app
COPY build/libs/*.jar ./app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]