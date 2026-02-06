# Stage 1: build with Maven (JDK 17)
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only Maven files first to leverage layer cache for dependencies
COPY pom.xml .
COPY .mvn .mvn
# If you use mvnw, copy it as well:
COPY mvnw .
RUN mvn -B -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -B -DskipTests package

# Stage 2: runtime image (JRE 17)
FROM eclipse-temurin:17-jre-jammy
ARG JAR_FILE=target/*.jar
WORKDIR /app

# Copy the jar produced by the builder stage
COPY --from=build /workspace/target/*.jar ./app.jar

# (Optional) create non-root user
RUN groupadd -r app && useradd -r -g app app || true
USER app

# Allow overriding java options
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
