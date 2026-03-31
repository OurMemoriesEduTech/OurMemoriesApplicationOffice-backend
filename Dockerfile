#FROM ubuntu:latest
#LABEL authors="Acer"
#
#ENTRYPOINT ["top", "-b"]
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/OurMemoriesEduSmart-0.0.1-SNAPSHOT.war app.war
EXPOSE ${PORT:-8080}
ENTRYPOINT ["java", "-jar", "app.war"]