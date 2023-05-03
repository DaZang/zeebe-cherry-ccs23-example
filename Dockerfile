# docker build -t zeebe-cherry-officepdf:1.0.0 .
FROM openjdk:17-alpine
EXPOSE 9091
COPY target/zeebe-cherry-simpleexample-*-jar-with-dependencies.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar", "io.camunda.CherryApplication"]

