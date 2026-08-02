FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw -DskipTests package

FROM eclipse-temurin:21-jre-alpine

RUN apk upgrade --no-cache && addgroup -S app && adduser -S -G app app

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

USER app

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
