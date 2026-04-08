# ── Stage 1: Build ──
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Stage 2: Run ──
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN addgroup --system next2view && adduser --system --group next2view
COPY --from=build /app/target/*.jar app.jar
RUN chown next2view:next2view app.jar
USER next2view
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
