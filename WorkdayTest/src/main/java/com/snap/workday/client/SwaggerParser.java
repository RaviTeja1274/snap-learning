package com.snap.workday.client;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class SwaggerParser {


    public void parse(String uri) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("Accept", "application/json");
        String swaggerContent = client.execute(httpGet, new BasicResponseHandler());
        client.close();
        process(swaggerContent);
    }

    private String service;
    private String version;
    private String token;
    public void setService(String service) {
        this.service = service;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void process(String content) throws IOException {
        SwaggerParseResult result = new OpenAPIParser().readContents(content, null, null);
        OpenAPI openAPI = result.getOpenAPI();
        AtomicInteger num = new AtomicInteger(1);
        Map<Integer, String> det = new HashMap<>();
        Map<Integer, HttpMethod> method = new HashMap<>();

        openAPI.getPaths().forEach((k, v) -> {
            Set<HttpMethod> httpMethods = v.readOperationsMap().keySet();
            httpMethods.forEach(httpMethod -> {
                System.out.println(num + ": " + httpMethod + " " + k);
                det.put(num.get(), k);
                method.put(num.get(), httpMethod);
                num.getAndIncrement();
            });
        });

        Scanner scanner = new Scanner(System.in);
        System.out.println("select path number :\n");
        String number = scanner.nextLine();

        AtomicReference<String> pathToCall = new AtomicReference<>(
            det.get(Integer.parseInt(number)));

        PathItem pathItem = openAPI.getPaths().get(pathToCall.get());
        HttpMethod httpMethod = method.get(Integer.parseInt(number));
        Operation operationCallToMake = pathItem.readOperationsMap().get(httpMethod);
        Map<String, String> queryParams = new HashMap<>();

        if (operationCallToMake.getParameters() != null) {
            operationCallToMake.getParameters().forEach(parameter -> {
                if ("path".equalsIgnoreCase(parameter.getIn())) {
                    pathToCall.set(pathToCall.get().replace('{' + parameter.getName() + '}', "1"));
                }
                if ("query".equalsIgnoreCase(parameter.getIn())) {
                    queryParams.put(parameter.getName(), "val");
                }
            });
        }

        System.out.println(pathToCall.get());
        HttpCommon recruitingV3 = new HttpCommon(token, service, version, pathToCall.get());
        switch (httpMethod) {
            case GET:
                recruitingV3.makeGetCall(null);
                break;
            case POST:
                recruitingV3.makePostCall("{}");
                break;
            case PUT:
                break;
            case HEAD:
                break;
            case PATCH:
                break;
            case DELETE:
                break;
            case OPTIONS:
                break;

        }
    }

    public void parse() throws IOException, URISyntaxException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL is = classloader.getResource("swagger/recruiting_v3.json");
        Path path = Paths.get(is.toURI());
        String lines = Files.readString(path);
        process(lines);
    }

}
