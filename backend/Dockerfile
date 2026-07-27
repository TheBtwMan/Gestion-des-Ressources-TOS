# Stage 1: Build stage
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app/backend

# Copy Maven wrapper files and pom.xml
COPY backend/mvnw .
COPY backend/.mvn .mvn
COPY backend/pom.xml .

RUN chmod +x ./mvnw

# Copy source code and test directory (referenced in pom.xml)
COPY backend/src ./src
COPY TestsUnitaires /app/TestsUnitaires

# Compile and package application
RUN ./mvnw clean package -DskipTests

# Stage 2: Production runtime stage
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy the built JAR artifact from build stage
COPY --from=build /app/backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
