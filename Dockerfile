# ==================== BUILD STAGE ====================
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy Maven Wrapper and pom.xml first (for better caching)
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

# Fix execute permission for Maven Wrapper
RUN chmod +x mvnw

# Copy source code
COPY src src

# Build the application (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# ==================== RUNTIME STAGE ====================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built WAR file from build stage
COPY --from=build /app/target/OurMemoriesEduSmart-0.0.1-SNAPSHOT.war app.war

# Expose port (Railway will override with $PORT)
EXPOSE ${PORT:-8080}

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.war"]