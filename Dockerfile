FROM openjdk:23-jdk-slim
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} live-data-service.jar
ENTRYPOINT ["java","-jar","/live-data-service.jar"]