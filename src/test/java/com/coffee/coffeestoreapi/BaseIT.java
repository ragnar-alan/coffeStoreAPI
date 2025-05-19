package com.coffee.coffeestoreapi;


import io.restassured.RestAssured;
import org.approvaltests.core.Scrubber;
import org.approvaltests.scrubbers.RegExScrubber;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.ResourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Testcontainers
@Sql("/test-data.sql")
@ActiveProfiles("it")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIT {

    public static Scrubber DATE_TIME_SCRUBBER = new RegExScrubber("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}", n -> String.format("[DateTime_%d]", n));

    protected static String getFileContents(String filename) throws IOException {
        var file = ResourceUtils.getFile(String.format("classpath:%s", filename));
        return FileUtils.readFileToString(file, UTF_8);
    }
}
