FROM openjdk:11-jre-slim as builder
WORKDIR /app
COPY *.jar .
RUN mkdir -p extracted && for jar in *.jar; do java -Djarmode=layertools -jar $jar extract --destination extracted; done

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]