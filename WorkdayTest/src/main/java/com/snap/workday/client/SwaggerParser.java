package com.snap.workday.client;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FileUtils;

public class SwaggerParser {


    public void parse() throws IOException, URISyntaxException {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL is = classloader.getResource("swagger/recruiting_v3.json");

        Path path = Paths.get(is.toURI());
        String lines = Files.readString(path);
        SwaggerParseResult result = new OpenAPIParser()
            .readContents(lines, null, null);
        OpenAPI openAPI = result.getOpenAPI();
        openAPI.getPaths()
            .forEach((k,v) -> {
                System.out.println(k);
//                System.out.println(v);
            });
    }

}
