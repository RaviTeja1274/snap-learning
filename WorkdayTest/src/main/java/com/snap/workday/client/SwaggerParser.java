package com.snap.workday.client;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.converter.SwaggerConverter;
import io.swagger.v3.parser.core.models.ParseOptions;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class SwaggerParser {


    public void parse(String uri) throws IOException, URISyntaxException {
//        CloseableHttpClient client = HttpClients.createDefault();
//
//        HttpGet httpGet = new HttpGet(uri);
//        httpGet.setHeader("Accept", "application/json");
//        String swaggerContent = client.execute(httpGet, new BasicResponseHandler());
//        client.close();
//        process(swaggerContent);
        parse();
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
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setFlatten(true);
        parseOptions.setResolveFully(true);
        parseOptions.setValidateExternalRefs(true);
        SwaggerParseResult result = new OpenAPIParser().readContents(content, null, parseOptions);

        SwaggerConverter swaggerConverter = new SwaggerConverter();
        SwaggerParseResult swaggerParseResult = swaggerConverter.readContents(content, null,
            parseOptions);
        OpenAPI openAPI = swaggerParseResult.getOpenAPI();

//        OpenAPI openAPI = result.getOpenAPI();
        AtomicInteger num = new AtomicInteger(1);
        Map<Integer, String> det = new HashMap<>();
        Map<Integer, HttpMethod> method = new HashMap<>();

        openAPI.getPaths().forEach((k, v) -> {
            Map<HttpMethod, Operation> httpMethods = v.readOperationsMap();
            httpMethods.forEach((httpMethod, operation) -> {
                System.out.println(num + ": " + httpMethod + " " + k + " " + operation.getSummary());
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

        final Operation operationCallToMake = pathItem.readOperationsMap().get(httpMethod);
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
                recruitingV3.makeGetCall(queryParams);
                break;
            case POST:
                RequestBody requestBody = operationCallToMake.getRequestBody();
                Schema schema = requestBody.getContent()
                    .get("*/*")
                    .getSchema();
                parseRequestSchema(schema,openAPI);
                System.exit(0);
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


    private Schema getSchemaForRef(String ref, OpenAPI openAPI){
        Map<String, Schema> componentSchemas = openAPI.getComponents().getSchemas();
        String schemaName = ref.substring(ref.lastIndexOf('/')+1);
        return componentSchemas.get(schemaName);
    }
    private void parseRequestSchema(Schema schema,  OpenAPI openAPI){
        Map<String,Object> propAndType = new HashMap<>();
        if (schema.get$ref() != null){
            String ref = schema.get$ref();
            Schema schemaFromComponent = getSchemaForRef(ref,openAPI);
            System.out.println(schemaFromComponent);
            Map<String, Object> props = parseSchemaAndLoadProperties(schemaFromComponent,
                propAndType, openAPI);
            System.out.println(props);
        }
    }

    private Map<String,Object> parseSchemaAndLoadProperties(Schema schemaFromComponent,
        Map<String, Object> propAndType, OpenAPI openAPI) {
        Map<String,Schema> properties = schemaFromComponent.getProperties();
        properties.forEach(new BiConsumer<String, Schema>() {
            @Override
            public void accept(String property, Schema schema) {
                System.out.println(property);
                if (schema.get$ref() != null){
                    Schema schemaFromComponent = getSchemaForRef(schema.get$ref(),openAPI);
                    Map<String, Object> data = parseSchemaAndLoadProperties(
                        schemaFromComponent, new HashMap<>(), openAPI);
                    propAndType.put(property, data);
                } else if (schema instanceof ArraySchema){
                    Schema items = schema.getItems();
                    Schema schemaFromComponent = getSchemaForRef(items.get$ref(),openAPI);
                    Map<String, Object> data = parseSchemaAndLoadProperties(
                        schemaFromComponent, new HashMap<>(), openAPI);
                    propAndType.put(property, Collections.singletonList(data));
                } else {
                    propAndType.put(property, schema.getType());
                }
                System.out.println("=================================");
            }
        });
        return propAndType;
    }

    public void parse() throws IOException, URISyntaxException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL is = classloader.getResource("swagger/recruiting_v3.json");
        Path path = Paths.get(is.toURI());
        String lines = Files.readString(path);
        process(lines);
    }

}
