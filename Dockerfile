# Use a base image with Java 21
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/coffeeStoreAPI-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080 5005

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]