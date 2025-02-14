## Scaling a spring-boot application with java-virtual-threads

### => problem statement:-
- spring request response model, java I/O uses 1 thread per request
- To handle more requests you had to create more threads (operating system threads) and that wasn't so easy to do it uses a lot of RAM, and ultimately you hit the limit.
### => probable solutions:-
- To go reactive, which offload blocking operations, but its complexity limits mainstream adoption
- To use non-blocking I/O something like GO routines
- To make thread creation cheap, and here java gives us virtual threads

---
### project-details:-
- It's a demo project to show how we can scale a java-app without using expensive thread creation or using any reactive programming paradigm(which is very difficult to implement and maintain for the large application). We will be using java's virtual threads (developed under project; Loom)

### project-setup: 
- project-setup using spring-boot-cli tool [﻿spring-boot-cli](https://docs.spring.io/spring-boot/cli/index.html) :-

```bash
spring init \
--artifactId=webapp-service \
--boot-version=3.2.5 \
--build=maven \
--dependencies=web \
--description="Demo project to scale a web application using java virtual threads" \
--groupId=io.sds \
--java-version=21 \
--language=java \
--name=webapp-demo-proj \
--packaging=jar \
--package-name=com.webapp \
--version=0.0.1-SNAPSHOT \
webapp-scaling-demo-proj
```
OR
```bash
git clone https://github.com/akikr/webapp-scaling-demo-proj.git
```
---

#### The project uses SDKMAN for managing Java and Maven versions.

To install SDKMAN refer: [sdkman.io](https://sdkman.io/install)

```bash
cd webapp-scaling-demo-proj;
#make a file .sdkmanrc
touch .sdkmanrc
```

File [.sdkmanrc](.sdkmanrc)
```text
# Enable auto-env through the sdkman_auto_env config
# Add key=value pairs of SDKs to use below
java=21.0.2-open
maven=3.9.9
```

Initialize your development environment using SDKMAN CLI:

```bash
sdk env install
sdk env
```

---
- Build project:-
```bash
./mvnw clean package
#OR
docker compose -f docker-compose.yaml up --build
```
- Run project [local]:-
```bash
docker compose -f docker-compose.yaml up;docker compose -f docker-compose.yaml down
```
---
 Dockerfile [A multi-stage Dockerfile]:-

```dockerfile
# Set the base-image for build stage
FROM maven:3-eclipse-temurin-21-alpine AS build
# Set up working directory
RUN mkdir -p /usr/app
COPY . /usr/app
WORKDIR /usr/app
# Build the application
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package
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
# Define environment variables for java-options and application-arguments
ENV JAVA_OPTS=""
ENV APP_ARGS=""
# Build the application start-up script
RUN echo 'java ${JAVA_OPTS} -jar webapp-service.jar ${APP_ARGS}' > ./start-app.sh
# Set a non-root user: Add a system group 'appgroup' and a system user 'appuser' in this group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown -R appuser:appgroup /usr/webapp
RUN chmod +x /usr/webapp/start-app.sh
USER appuser
# Expose the application port
EXPOSE 8090
# Run using start-up script
CMD ["sh", "-c", "./start-app.sh"]

```

A docker compose file using `traefik` as a loadbalancer and api-proxy to route the http request to webapp-service

docker-compose.yaml:-

```yaml
services:
  httpbin-service:
    image: docker.io/mccutchen/go-httpbin:latest
    ports:
      - "8080"
    deploy :
      mode : replicated
      replicas : 1

  webapp-service:
    build:
      context: .
      dockerfile: Dockerfile
    image: webapp-service:v1
    environment:
      - APP_ARGS=--spring.profiles.active=dev --spring.threads.virtual.enabled=false --http-bin.server.url=http://httpbin-service:8080
      #- JAVA_OPTS=-Xmx300m
    ports:
      - "8090"
    labels:
      - "traefik.enable=true"
      # Route all the requests to 'webapp-service' via web-entrypoint on http port 80
      - "traefik.http.routers.webapp-service.entrypoints=web"
      # Route all the request with path-regex /api/* to this webapp-service
      - "traefik.http.routers.webapp-service.rule=PathPrefix(`/api`)"
      - "traefik.http.services.webapp-service.loadbalancer.server.port=8090"
      # Strip /api prefix before forwarding to the service
      - "traefik.http.middlewares.strip-api-prefix.stripprefix.prefixes=/api"
      - "traefik.http.routers.webapp-service.middlewares=strip-api-prefix"
    deploy:
      mode: replicated
      replicas: 1
      resources:
        limits:
          cpus: "2.0"
          memory: "200M"
        reservations:
          cpus: "1.0"
          memory: "100M"
    depends_on:
      httpbin-service:
        condition: service_started

  api-proxy:
    image: traefik:v3.1
    container_name: web-app-proxy
    command:
      # Enables access logging
      - "--log.level=INFO"
      - "--accesslog=true"
      # Do not expose any docker containers unless explicitly told so
      - "--providers.docker.exposedbydefault=false"
      # Enables the web-entrypoint on http port 80
      - "--entrypoints.web.address=:80"
    ports:
      - "80:80"
    volumes:
      # So that Traefik can listen to the Docker events
      - /var/run/docker.sock:/var/run/docker.sock
    depends_on:
      httpbin-service:
        condition: service_started
      webapp-service:
        condition: service_started

```
- Run and see logs for webapp-service using docker-compose:-
```bash
docker compose up -d;docker compose logs -f webapp-service;docker compose down 
```
---
- Testing tools:-
    - Use a [﻿httpbin.org](https://httpbin.org/) as a server-service in docker-container: `mccutchen/go-httpbin:latest` 
    - Use a HTTP load generator: [﻿oha](https://github.com/hatoo/oha) to run a load test


- Load test results [Deployed using docker compose] :-
```yaml
# With deployment configs:-
  deploy:
    mode: replicated
    replicas: 1
    resources:
      limits:
        cpus: "2.0"
        memory: "200M"
      reservations:
        cpus: "1.0"
        memory: "100M"
```
- with traditional java-threads:-
```
❯ oha -c 50 -n 100 http://localhost/api/webapp/delay/3
Summary:
  Success rate: 100.00%
  Total:	151.0207 secs
  Slowest:	75.5640 secs
  Fastest:	3.0843 secs
  Average:	57.3960 secs
  Requests/sec:	0.6622
```
- with virtual java-threads:-
```
❯ oha -c 50 -n 100 http://localhost/api/webapp/delay/3
Summary:
  Success rate: 100.00%
  Total:	6.1450 secs
  Slowest:	3.1207 secs
  Fastest:	3.0107 secs
  Average:	3.0643 secs
  Requests/sec:	16.2735
```

reference: [**﻿spring-tips-virtual-threads**](https://youtube.com/watch?v=9iH5h11YJak) & [**﻿the-magic-behind-virtual-threads**](https://www.youtube.com/watch?v=HQsYsUac51g)**﻿**
