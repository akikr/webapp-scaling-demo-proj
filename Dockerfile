FROM maven:3-eclipse-temurin-21-alpine AS build
RUN mkdir -p /usr/app
COPY . /usr/app
WORKDIR /usr/app
RUN ./mvnw clean package
RUN jdeps --ignore-missing-deps -q \
    --recursive \
    --multi-release 21 \
    --print-module-deps \
    --class-path 'target/dependencies/*' \
    target/webapp-service-*.jar > deps.info
RUN jlink --add-modules $(cat deps.info) \
    --no-header-files \
    --no-man-pages \
    --output /webapp-jre

FROM alpine:latest@sha256:77726ef6b57ddf65bb551896826ec38bc3e53f75cdde31354fbffb4f25238ebd

ENV JAVA_HOME /usr/java/app-jre21
ENV PATH $JAVA_HOME/bin:$PATH
COPY --from=build /webapp-jre $JAVA_HOME

RUN mkdir -p /usr/webapp
COPY --from=build /usr/app/target/webapp-service-*.jar /usr/webapp/app.jar
WORKDIR /usr/webapp

# Add a system group 'appgroup' and a system user 'appuser' in this group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown -R appuser:appgroup /usr/webapp
USER appuser

# Set environment variables for java-options and application-arguments
ENV JAVA_OPTS "-Xmx512m"
ENV APP_ARGS "--spring.profiles.active=dev"

EXPOSE 8090

CMD ["/bin/sh", "-c", "java $JAVA_OPTS -jar app.jar $APP_ARGS"]