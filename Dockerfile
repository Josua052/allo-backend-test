# Multi-stage build for Allo Bank Backend Challenge
# Stage 1: Build dependencies and source code
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy maven settings to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q

# Copy the actual source code and package to JAR
COPY src/ src/
RUN ./mvnw package -DskipTests -q

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Copy only the compiled jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Required by Allo Bank test rules
EXPOSE 4110

# Entrypoint to run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
