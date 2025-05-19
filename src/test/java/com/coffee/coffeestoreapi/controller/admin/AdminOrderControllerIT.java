package com.coffee.coffeestoreapi.controller.admin;

import com.coffee.coffeestoreapi.BaseIT;
import io.restassured.RestAssured;
import org.approvaltests.core.Options;
import org.approvaltests.scrubbers.Scrubbers;
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

import static org.approvaltests.JsonApprovals.verifyJson;

public class AdminOrderControllerIT extends BaseIT {
    private static final DockerImageName IMAGE_NAME = DockerImageName
            .parse("postgres:16-alpine")
            .asCompatibleSubstituteFor("postgres");
    @Container
    static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>(IMAGE_NAME)
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
    void testGetOrderShouldPass() {
        var result = RestAssured
                .given()
                .when()
                    .get("/api/v1/admin/orders/{orderNumber}", "ORD-1001")
                .then()
                    .statusCode(200)
                    .extract()
                    .asString();
        verifyJson(result);
    }

    @Test
    void testGetOrderShouldFail_dueToMissingResource() {
        RestAssured
                .given()
                .when()
                    .get("/api/v1/admin/orders/{orderNumber}", "prkr")
                .then()
                    .statusCode(404)
                    .extract()
                    .asString();
    }

    @Test
    void testGetOrdersShouldPass() {
        RestAssured
                .given()
                .when()
                    .get("/api/v1/admin/orders/list")
                .then()
                    .statusCode(200)
                    .extract()
                    .asString();
    }

    @Test
    void testPatchOrdersShouldPass() throws IOException {
        var result = RestAssured
                .given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(getFileContents("mockRequests/mockPatchOrderRequest.json"))
                .when()
                    .patch("/api/v1/admin/orders/{orderNumber}", "ORD-1004")
                .then()
                    .statusCode(200)
                    .extract()
                    .asString();
        verifyJson(result);
    }

    @Test
    void testPatchOrdersShouldFail_dueToRequestObjectValidationMissingOrderLines() throws IOException {
        var result = RestAssured
                .given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(getFileContents("mockRequests/mockPatchOrderRequestMissingOrderLines.json"))
                .when()
                    .patch("/api/v1/admin/orders/{orderNumber}", "ORD-1004")
                .then()
                    .statusCode(400)
                    .extract()
                    .asString();
        verifyJson(result);
    }

    @Test
    void testPatchOrdersShouldFail_dueToTooLongOrdererName() throws IOException {
        var result = RestAssured
                .given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(getFileContents("mockRequests/mockPatchOrderRequestTooLongOrdererName.json"))
                .when()
                    .patch("/api/v1/admin/orders/{orderNumber}", "ORD-1004")
                .then()
                    .statusCode(400)
                    .extract()
                    .asString();
        verifyJson(result);
    }

    @Test
    void testPatchOrdersShouldFail_dueToNotExistingOrder() throws IOException {
        var result = RestAssured
                .given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(getFileContents("mockRequests/mockPatchOrderRequest.json"))
                .when()
                    .patch("/api/v1/admin/orders/{orderNumber}", "misssing-order-number")
                .then()
                    .statusCode(404)
                    .extract()
                    .asString();
        verifyJson(result);
    }

    @Test
    void testDeleteOrdersShouldPass() {
        RestAssured
                .given()
                .when()
                    .delete("/api/v1/admin/orders/{orderNumber}", "ORD-1010")
                .then()
                    .statusCode(204)
                    .extract()
                    .asString();

        //Verifying that the order is soft-deleted (status is set to cancelled and the cancelled_at date is populated)
        var result = RestAssured
                .given()
                .when()
                    .get("/api/v1/admin/orders/{orderNumber}", "ORD-1010")
                .then()
                    .statusCode(200)
                    .extract()
                    .asString();
        verifyJson(result, new Options().withScrubber(DATE_TIME_SCRUBBER));
    }
}
