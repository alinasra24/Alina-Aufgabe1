# Use an official Gradle image to build the Spring Boot app
FROM gradle:8.1.1-jdk17 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy only the Gradle wrapper and build files first to leverage Docker layer caching
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./

# Ensure the Gradle wrapper is executable
RUN chmod +x ./gradlew

# Download dependencies before copying the entire project to leverage Docker cache
RUN ./gradlew dependencies --no-daemon

# Copy the rest of the application code
COPY . .

# Build the Spring Boot application
RUN ./gradlew bootJar --no-daemon

# Use an official OpenJDK runtime as a base image for running the application
FROM openjdk:17-jdk-slim

# Install curl for health-checks or other dependencies
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Set the working directory inside the container
WORKDIR /app

# Copy the built application from the previous stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the port Spring Boot will run on (default 7979 or whatever your app uses)
EXPOSE 7979

# Set the entry point for the container to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
