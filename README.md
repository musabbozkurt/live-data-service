<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary><h2 style="display: inline-block">Table of Contents</h2></summary>
  <ol>
    <li><a href="#Summary">Summary</a></li>
    <li><a href="#Prerequisites">Prerequisites and Installation</a></li>
    <li><a href="#Tech_Stack">Tech Stack</a></li>
    <li><a href="#How_To_Run_And_Test_Application">How To Run And Test Application</a></li>
    <li><a href="#How_To_Run_And_Test_Dockerfile">How To Run And Test Application with Dockerfile (OPTIONAL)</a></li>
  </ol>
</details>

### Summary

#### live-data-service is providing CRUD operations to process live events

### Prerequisites

- Java 8+ needs to be installed
- Maven needs to be installed
- Install any Java IDE (Eclipse, STS, Intellij etc..) and ensure you are able to launch
- Clone or checkout the project from version control system (git) and follow below steps

### Tech_Stack

- Java 8+
- Spring Boot: 2.7.5
- H2 Database (Default values are provided below)
    - username: sa
    - password: sa
    - url: jdbc:h2:mem:SPORT_RADAR;DB_CLOSE_DELAY=-1
    - Default H2 Database url: http://localhost:8080/h2-console
- Swagger2 - For API Local Testing
    - Default Swagger url: http://localhost:8080/swagger-ui.html
- Flyway for database migration
- Centralize exception handling by ControllerAdvice
- Mapstruct to map different type of objects to each other
- Zipkin and Sleuth dependencies were added to track the logs easily

### How_To_Run_And_Test_Application

- Please follow the following steps, if you want to build and run Spring Boot Application

```
*** Run the application by following these steps.

1 - cd live-data-service
2 - mvn clean install
3 - mvn spring-boot:run

*** Test the application by following these steps.

$ Default Swagger url: http://localhost:8080/swagger-ui.html
$ Default H2 Database url: http://localhost:8080/h2-console

$ Use Swagger and click on Try it out button and fill the input payload according to the contract.
```

### [Docker can be installed (OPTIONAL)](https://spring.io/guides/topicals/spring-boot-docker/)

### How_To_Run_And_Test_Dockerfile

- Please follow the following steps, if you want to build and run Dockerfile

```
1 - cd live-data-service
2 - mvn clean install --------THIS IS MUST---------
3 - docker build -t springio/live-data-service .
4 - docker run -p 8080:8080 springio/live-data-service

*** Test the application by following these steps.

$ Default Swagger url: http://localhost:8080/swagger-ui.html
$ Default H2 Database url: http://localhost:8080/h2-console

$ Use Swagger and click on Try it out button and fill the input payload according to the contract.
```

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.5/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.5/maven-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.7.5/reference/htmlsingle/#web)

### Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
