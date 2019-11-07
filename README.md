# Money transfer API

A Java RESTful API for money transfers between accounts

### Technologies
- JAX-RS API
- H2 in memory database
- Log4j
- Jetty Container (for Test and Demo app)
- Apache HTTP Client


### How to run
```sh
mvn clean install
mvn exec:java
```

Application starts a jetty server on localhost port 8080 An H2 in memory database initialized with some sample user and account data To view

- http://localhost:8080/user/all
- http://localhost:8080/account/all
- http://localhost:8080/account/1

Refer to collection.json for postman collection. Import it into postman to use the available api's.

### Http Status
- 200 OK: The request has succeeded
- 400 Bad Request: The request could not be understood by the server 
- 404 Not Found: The requested resource cannot be found
- 500 Internal Server Error: The server encountered an unexpected condition 