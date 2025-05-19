package com.coffee.coffeestoreapi.controller;


import com.coffee.coffeestoreapi.BaseIT;
import io.restassured.RestAssured;
import org.approvaltests.JsonApprovals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;


public class OrderControllerIT extends BaseIT {
    private static final DockerImageName IMAGE_NAME = DockerImageName
            .parse("postgres:16-alpine")
            .asCompatibleSubstituteFor("postgres");

    @Container
    protected static final PostgreSQLContainer POSTGRES_CONTAINER = new PostgreSQLContainer<>(IMAGE_NAME)
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");


    @LocalServerPort
    private int port;

    static {
        POSTGRES_CONTAINER.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void testOrderCreation() throws IOException {
        RestAssured
                .given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(getFileContents("mockRequests/mockCreateOrderRequest.json"))
                .when()
                    .post("/api/v1/orders")
                .then()
                    .statusCode(201)
                    .extract()
                    .asString();
    }

    @Test
    void testOrderCreationShouldFail_dueToMissingDrinkOrderLine() throws IOException {
        var result = RestAssured
                .given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(getFileContents("mockRequests/mockCreateOrderRequest_noDrinkOrderLine.json"))
                .when()
                    .post("/api/v1/orders")
                .then()
                    .statusCode(400)
                    .extract()
                    .asString();
        JsonApprovals.verifyJson(result);
    }

    @Test
    void testOrderCreation_shouldFailDueToMissingOrderLines() throws IOException {
        var result = RestAssured
                .given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(getFileContents("mockRequests/mockCreateOrderRequest_noOrderLines.json"))
                .when()
                    .post("/api/v1/orders")
                .then()
                    .statusCode(400)
                    .extract()
                    .asString();
        JsonApprovals.verifyJson(result);
    }

    @Test
    void testOrderCreation_shouldFailDueToLongOrdererName() throws IOException {
        var result = RestAssured
                .given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(getFileContents("mockRequests/mockCreateOrderRequest_toLongOrdererName.json"))
                .when()
                    .post("/api/v1/orders")
                .then()
                    .statusCode(400)
                    .extract()
                    .asString();
        JsonApprovals.verifyJson(result);
    }

// I was not able to get this test to work, because the validation is not triggered for some reason
    @Test
    void testOrderCreation_shouldFailDueToPriceIsNull() throws IOException {
        var result = RestAssured
                .given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(getFileContents("mockRequests/mockCreateOrderRequest_missingtotalPrice.json"))
                .when()
                    .post("/api/v1/orders")
                .then()
                    .statusCode(400)
                    .extract()
                    .asString();
        JsonApprovals.verifyJson(result);
    }
}
