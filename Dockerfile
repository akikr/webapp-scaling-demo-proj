# Set the base-image for build stage
FROM maven:3-eclipse-temurin-21-alpine AS build
# Set up working directory
RUN mkdir -p /usr/app
COPY . /usr/app
WORKDIR /usr/app
# Build the application
RUN ./mvnw clean package
# Build the application specific JRE
RUN jdeps --ignore-missing-deps -q \
    --recursive \
    --multi-release 21 \
    --print-module-deps \
    --class-path 'target/dependencies/*' \
    target/*.jar > modules.info
# Add 'jdk.management' module for JDK-specific management interfaces for the JVM while building application specific JRE
RUN jlink --add-modules jdk.management,$(cat modules.info) \
    --no-header-files \
    --no-man-pages \
    --output /app-jre

# Set the base-image for final stage
FROM alpine:latest@sha256:77726ef6b57ddf65bb551896826ec38bc3e53f75cdde31354fbffb4f25238ebd
# Set JAVA_HOME using application specific JRE from build-stage
ENV JAVA_HOME /usr/lib/java/jre
ENV PATH $JAVA_HOME/bin:$PATH
COPY --from=build /app-jre $JAVA_HOME
# Copy the artifact from build-stage
RUN mkdir -p /usr/webapp
COPY --from=build /usr/app/target/*.jar /usr/webapp/webapp-service.jar
WORKDIR /usr/webapp
# Copy the appliaction start-up script
COPY start-app.sh .
# Set a non-root user: Add a system group 'appgroup' and a system user 'appuser' in this group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown -R appuser:appgroup /usr/webapp
RUN chmod +x /usr/webapp/start-app.sh
USER appuser
# Define environment variables for java-options and application-arguments
ENV JAVA_OPTS ''
ENV APP_ARGS ''
# Expose the application port
EXPOSE 8090
# Run using start-up script
CMD [ "./start-app.sh" ]
