FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
ADD ./target/spring-boot-rest-service-cors-0.1.7.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
EXPOSE 8081