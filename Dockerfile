# Multi-stage build for Spring Boot application
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

# Create app directory
WORKDIR /app

# Install wget for healthcheck and create non-root user
RUN apt-get update && apt-get install -y wget && \
    groupadd -g 1001 appuser && \
    useradd -u 1001 -g appuser appuser

# Copy jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application with optimized production JVM settings
ENTRYPOINT ["java", \
    "-server", \
    "-Xms512m", \
    "-Xmx2g", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=100", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UnlockExperimentalVMOptions", \
    "-XX:+UseCGroupMemoryLimitForHeap", \
    "-XX:+AlwaysPreTouch", \
    "-XX:+DisableExplicitGC", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:HeapDumpPath=/app/logs/", \
    "-Dspring.profiles.active=prod", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dlogging.config=classpath:logback-spring.xml", \
    "-jar", "app.jar"]