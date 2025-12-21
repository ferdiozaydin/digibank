# Build stage
# Use Eclipse Temurin because the old openjdk image was removed from Docker Hub.
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY src ./src
RUN mkdir -p lib && curl -L -o lib/postgresql.jar https://jdbc.postgresql.org/download/postgresql-42.7.4.jar
RUN mkdir bin
RUN javac -cp lib/postgresql.jar -d bin -sourcepath src src/com/digibank/Main.java

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/bin ./bin
COPY --from=build /app/lib ./lib
CMD ["java", "-cp", "bin:lib/postgresql.jar", "com.digibank.Main"]
