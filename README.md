# Coffee Store API

A Spring Boot application for managing coffee orders with discount capabilities.

## Project Overview

The Coffee Store API is a RESTful service that allows users to create, retrieve, and manage coffee orders. It includes features such as:

- Creating new coffee orders
- Retrieving order details
- Listing all orders
- Applying discounts based on configurable rules

## Prerequisites

- Java 21
- Maven 3.6+
- Docker and Docker Compose (optional, for containerized deployment)
- PostgreSQL (automatically set up with Docker Compose)

## Building the Application

To build the application, run:

```bash
mvn clean package
```

This will compile the code, run tests, and create a JAR file in the `target` directory.

## Running the Application Locally

### Option 1: Using Maven

1. Set up a PostgreSQL database and configure the connection details:

```bash
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/coffee_store
set SPRING_DATASOURCE_USERNAME=user
set SPRING_DATASOURCE_PASSWORD=password
```

2. Run the application:

```bash
mvn spring-boot:run
```

### Option 2: Using Java

1. Set up a PostgreSQL database and configure the connection details as environment variables (same as above).

2. Run the JAR file:

```bash
java -jar target/coffeeStoreAPI-0.0.1-SNAPSHOT.jar
```

### Option 3: Using Docker Compose

The easiest way to run the application is using Docker Compose, which will set up both the application and the PostgreSQL database:

1. Build the application:

```bash
mvn clean package
```

2. Start the containers:

```bash
docker-compose up -d
```

This will start both the application and the PostgreSQL database in containers.

## Debugging the Application

### Local Debugging

When running the application locally with Maven, you can enable debug mode:

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

Then connect your IDE to port 5005.

### Remote Debugging with Docker

The Docker Compose configuration already includes remote debugging capabilities on port 5005. To use it:

1. Start the application with Docker Compose:

```bash
docker-compose up -d
```

2. Connect your IDE's remote debugger to localhost:5005.

## API Endpoints

The application exposes the following REST endpoints:

### Orders API

- `GET /api/v1/orders/{orderId}` - Get a specific order by ID
- `GET /api/v1/orders/list` - Get all orders
- `POST /api/v1/orders` - Create a new order

Example request for creating an order:

```json
{
  "order_lines": [
    {
      "item": {
        "type": "COFFEE",
        "name": "Latte"
      },
      "toppings": [
        {
          "name": "Caramel"
        }
      ],
      "quantity": 2
    }
  ]
}
```

## Configuration

The application uses environment variables for configuration:

- `SPRING_DATASOURCE_URL` - JDBC URL for the PostgreSQL database
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

Feature flags can be configured in the `application.yaml` file:

```yaml
discounts:
  enabled: true
  twenty-five-percent: true
  free-item-after-three: true
```