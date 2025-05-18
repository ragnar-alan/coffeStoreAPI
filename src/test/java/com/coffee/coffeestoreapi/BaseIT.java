package com.coffee.coffeestoreapi;


import org.approvaltests.core.Scrubber;
import org.approvaltests.scrubbers.RegExScrubber;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.ResourceUtils;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Testcontainers
@Sql("/test-data.sql")
@ActiveProfiles("it")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIT {

    public static Scrubber DATE_SCRUBBER = new RegExScrubber("\\d{4}-\\d{2}-\\d{2}", n -> String.format("[Date_%d]", n));
    public static Scrubber DATE_TIME_SCRUBBER = new RegExScrubber("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}", n -> String.format("[DateTime_%d]", n)
    );

    protected static String getFileContents(String filename) throws IOException {
        var file = ResourceUtils.getFile(String.format("classpath:%s", filename));
        return FileUtils.readFileToString(file, UTF_8);
    }
}
