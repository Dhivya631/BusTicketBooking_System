# Use official OpenJDK image
FROM openjdk:17-jdk-slim

# Set environment variable for better performance
ENV JAVA_OPTS=""

# Create a directory for the app
WORKDIR /app

# Copy jar file into the container
COPY target/* user-service.jar

# Expose port (match with server.port)
EXPOSE 8081

# Set environment variables (passwords will be passed securely from Docker Compose or environment)
ENV SPRING_PROFILES_ACTIVE=dev

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar user-service.jar"]
