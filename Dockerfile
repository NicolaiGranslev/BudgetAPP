# Use to run the app online (ex on azure or Render.com)
FROM maven:3.9-eclipse-temurin-21 AS build
COPY . /app
WORKDIR /app
RUN mvn -q package -DskipTests

FROM eclipse-temurin:21-jre
COPY --from=build /app/target/budget-app-1.0.jar app.jar
CMD ["java", "-jar", "app.jar"]
