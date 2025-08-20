# Build stage
FROM maven:3.8.6-amazoncorretto-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
# Build app
RUN mvn clean package -DskipTests

# Runtime stage
FROM amazoncorretto:17
# Install shadow-utils for group-, useradd + curl; not included with corretto
RUN yum install -y shadow-utils
RUN yum install -y curl && \
    yum clean all && \
    rm -rf /var/cache/yum
WORKDIR /app
# Create non-root user
RUN groupadd --system --gid 1001 springboot && \
    useradd --system --uid 1001 --gid 1001 springboot
# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership + switch user
RUN chown springboot:springboot app.jar
USER springboot
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]
