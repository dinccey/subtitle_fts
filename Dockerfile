# First stage: build the jar using Maven
FROM maven:3.9.4-amazoncorretto-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -Dmaven.test.skip

# Second stage: run the jar using Corretto
FROM amazoncorretto:17
COPY --from=builder /app/target/*.jar app.jar

RUN mkdir -p /mnt/data
RUN mkdir -p /usr/share/ssl_bundle

EXPOSE 8081
ENTRYPOINT ["java","-jar","/app.jar"]
