FROM fabric8/java-alpine-openjdk11-jre:1.6.5

ENV JAVA_APP_JAR questions-api-0.1.0-runner.jar
ENV AB_OFF true

EXPOSE 8080

ADD target/$JAVA_APP_JAR /deployments/
