ARG REDIS_HOST
ARG REDIS_PORT
ARG DB_URL
ARG DB_USERNAME
ARG DB_PASSWORD
ARG KAFKA_BROKER

ENV REDIS_HOST=$REDIS_HOST
ENV REDIS_PORT=$REDIS_PORT
ENV DB_URL=$DB_URL
ENV DB_USERNAME=$DB_USERNAME
ENV DB_PASSWORD=$DB_PASSWORD
ENV KAFKA_BROKER=$KAFKA_BROKER

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