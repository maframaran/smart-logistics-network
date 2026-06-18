FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY common/ ./common/
COPY fleet-service/ ./fleet-service/
RUN --mount=type=cache,target=/root/.m2 \
    mvn install -N --no-transfer-progress && \
    mvn install -f common/pom.xml -DskipTests --no-transfer-progress && \
    mvn package -f fleet-service/pom.xml -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/fleet-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
