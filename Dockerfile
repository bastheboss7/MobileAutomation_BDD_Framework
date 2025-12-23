# Dockerfile for running Mobile BDD Tests
# Use this for CI/CD environments

FROM maven:3.9-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src
COPY testngParallel.xml .

# Compile the project
RUN mvn clean compile test-compile -q

# ============================================================================
# Runtime Image
# ============================================================================
FROM maven:3.9-eclipse-temurin-21-alpine

LABEL maintainer="Baskar <baskar@example.com>"
LABEL description="Mobile Automation BDD Framework Test Runner"
LABEL version="4.0.0"

WORKDIR /app

# Copy built project
COPY --from=builder /app /app
COPY --from=builder /root/.m2 /root/.m2

# Default environment variables
ENV PLATFORM=android
ENV ENV=staging
ENV CUCUMBER_TAGS=@Smoke
ENV APPIUM_HUB=http://appium:4723

# Entry point for running tests
ENTRYPOINT ["sh", "-c", "mvn test -Dplatform=${PLATFORM} -Denv=${ENV} -Dcucumber.filter.tags=\"${CUCUMBER_TAGS}\" -DHUB=${APPIUM_HUB}"]
