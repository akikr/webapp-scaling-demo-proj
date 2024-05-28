FROM maven:3-eclipse-temurin-21-alpine AS build
RUN mkdir /usr/app
COPY . /usr/app
WORKDIR /usr/app
RUN ./mvnw clean package
RUN jar vxf target/webapp-service-*.jar
RUN jdeps --ignore-missing-deps -q \
    --recursive \
    --multi-release 21 \
    --print-module-deps \
    --class-path BOOT-INF/lib/* \
    target/webapp-service-*.jar > deps.info
RUN jlink --add-modules $(cat deps.info) \
    --strip-debug \
    --no-header-files \
    --no-man-pages \
    --output /webapp-jre

FROM alpine:3.20
ENV JAVA_HOME /usr/java/app-jre21
ENV PATH $JAVA_HOME/bin:$PATH
COPY --from=build /webapp-jre $JAVA_HOME

RUN mkdir /usr/webapp
COPY --from=build /usr/app/target/webapp-service-*.jar /usr/webapp/app.jar
WORKDIR /usr/webapp

ENV JAVA_OPTS ''

ENTRYPOINT java $JAVA_OPTS -jar app.jar