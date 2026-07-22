FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copy only the pom.xml (and any build wrappers/config) first so Docker can cache
# the downloaded dependencies layer. When application source files change this
# layer will be reused and dependencies won't be re-downloaded.
COPY pom.xml ./
COPY .mvn/ .mvn/

# Download dependencies offline into the image cache. Use -B (batch mode) and
# -ntp to avoid transfer progress which makes layer output cleaner.
# Use -T 1C to run Maven with one thread per CPU core for parallel dependency
# resolution/build where possible.
RUN mvn -B -ntp -T 1C -DskipTests dependency:go-offline

# Now copy the source and build. Changes in source files won't invalidate the
# previous dependencies layer.
COPY src ./src
# Build using one thread per core as well (-T 1C) to speed up multi-module
# compilations when applicable.
RUN mvn -B -ntp -T 1C package -DskipTests -Dmaven.test.skip=true

FROM gcr.io/distroless/java25-debian13:latest
WORKDIR /app
COPY --from=build /app/target/duty-tracker-backend-*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=production
EXPOSE 8080
CMD ["app.jar"]
