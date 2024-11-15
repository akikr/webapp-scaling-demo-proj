## Scaling a spring-boot application with java-virtual-threads

### => problem statement:-
- spring request response model, java I/O uses 1 thread per request
    - To handle more requests you had to create more threads (operating system threads) and that wasn't so easy to do it uses lot of RAM and ultimately you hit the limit.
---
### project-details:-
- It's a demo project to show how we can scale a java-app without using expensive thread creation or using any reactive programming paradigm(which is very difficult to implement and maintain for the a large application). We will be using java's virtual threads (developed under project; Loom)

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
```bash
cd webapp-scaling-demo-proj;
sdk use java 21.0.2-open;
```
```bash
./mvnw dependency:go-offline;
./mvnw clean;
```
---
- Build project:-
```bash
./mvnw clean package
```
- Run project [local]:-
```bash
./mvnw spring-boot:run
```
OR
```bash
java -jar target/webapp-service-0.0.1-SNAPSHOT.jar
```
---
- Build project using Dockerfile:-
```bash
docker build --no-cache -t webapp-service:v1 -f Dockerfile .
```
- Build custom nginx-service image:-
```bash
docker build -t nginx-service:v1 -f nginx-conf/Dockerfile nginx-conf/.
```

 Dockerfile:-

```dockerfile
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
```

docker-compose.yaml :-

```yaml
version: '3'

services:
  httpbin-service:
    image: mccutchen/go-httpbin:latest
    container_name: go-httpbin
    ports:
      - "8080:8080"

  webapp-service:
    image: webapp-service:v1
    container_name: webapp
    environment:
      - JAVA_OPTS=-Dspring.threads.virtual.enabled=true -Dhttp-bin.server.url=http://httpbin-service:8080
    ports:
      - "8090:8090"
    depends_on:
      - httpbin-service

```
- Run using docker-compose:-
```bash
docker-compose -f docker-compose.yaml up;
docker-compose -f docker-compose.yaml down;
```
---
- Testing tools:-
    - Use a [﻿httpbin.org](https://httpbin.org/) as a server-service in docker-container: `mccutchen/go-httpbin:latest` 
    - Use a HTTP load generator: [﻿oha](https://github.com/hatoo/oha) to run a load test


- Load test results [Intel-Core-i5 CPUS:8-Cores RAM:16GB]:-
- with traditional java-threads:-
```
❯ oha -c 50 -n 100 http://localhost:8090/delay/3
Summary:
  Success rate: 100.00%
  Total:        39.3084 secs
  Slowest:      27.2256 secs
  Fastest:      3.0096 secs
  Average:      14.9455 secs
  Requests/sec: 2.5440
```
- with virtual java-threads:-
```
❯ oha -c 50 -n 100 http://localhost:8090/delay/3
Summary:
  Success rate: 100.00%
  Total:        6.4676 secs
  Slowest:      3.3247 secs
  Fastest:      3.0512 secs
  Average:      3.1734 secs
  Requests/sec: 15.4616
```

reference: [**﻿spring-tips-virtual-threads**](https://youtube.com/watch?v=9iH5h11YJak) & [**﻿the-magic-behind-virtual-threads**](https://www.youtube.com/watch?v=HQsYsUac51g)**﻿**
