package com.coffee.coffeestoreapi.controller.admin;

import com.coffee.coffeestoreapi.BaseIT;
import io.restassured.RestAssured;
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

public class AdminProductControllerIT extends BaseIT {
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
    void testGetProductsShouldPass() {
        var result = RestAssured
                .given()
                .when()
                    .get("/api/v1/admin/products/list")
                .then()
                    .statusCode(200)
                    .extract()
                    .asString();
        verifyJson(result);
    }

    @Test
    void testGetProductShouldPass() {
        var result = RestAssured
                .given()
                .when()
                    .get("/api/v1/admin/products/{productId}", 1)
                .then()
                    .statusCode(200)
                    .extract()
                    .asString();
        verifyJson(result);
    }

    @Test
    void testGetProductShouldFail_dueToMissingResource() {
        RestAssured
                .given()
                .when()
                    .get("/api/v1/admin/products/{productId}", 534)
                .then()
                    .statusCode(404)
                    .extract()
                    .asString();
    }

    @Test
    void testCreateProductShouldPass() throws IOException {
        RestAssured
                .given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(getFileContents("mockRequests/mockCreateProductRequest.json"))
                .when()
                    .post("/api/v1/admin/products")
                .then()
                    .statusCode(201)
                    .extract()
                    .asString();
    }

    @Test
    void testGetMostPopularItemsShouldPass() {
        var result = RestAssured
                .given()
                .when()
                    .get("/api/v1/admin/products/most-popular")
                .then()
                    .statusCode(200)
                .extract()
                .asString();
        verifyJson(result);
    }
}
