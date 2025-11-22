FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the jar file
COPY target/leader-election-*.jar app.jar

# Expose port (will be overridden by application config if needed)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

