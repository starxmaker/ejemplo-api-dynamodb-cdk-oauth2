package dev.leosanchez.integration;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import dev.leosanchez.resources.ProductResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@QuarkusTest
@io.quarkus.test.junit.TestProfile(ProductResourceIT.TestProfile.class)
@TestHTTPEndpoint(ProductResource.class)
public class ProductResourceIT {

    private static Network containersNetwork = Network.newNetwork();

    @Container
    public static GenericContainer<?> dynamodb = new GenericContainer<>(
            DockerImageName.parse("amazon/dynamodb-local:1.11.477"))
            .withExposedPorts(8000)
            .withNetwork(containersNetwork)
            .withNetworkAliases("dynamodb")
            .withCommand("-jar DynamoDBLocal.jar -inMemory -sharedDb");

    @Container
    public static GenericContainer<?> createTableCommandContainer = new GenericContainer<>(
            DockerImageName.parse("amazon/aws-cli:2.0.6"))
            .withEnv(Map.of("AWS_ACCESS_KEY_ID", "MOCK", "AWS_SECRET_ACCESS_KEY", "MOCK"))
            .withNetwork(containersNetwork)
            .withFileSystemBind(System.getProperty("user.dir") + "/src/test/resources/ProductTableSchema.json",
            "/home/ProductTableSchema.json", BindMode.READ_ONLY)
            .withCommand(
                    "--endpoint-url=http://dynamodb:8000 --region=us-east-1 dynamodb create-table --cli-input-json file:///home/ProductTableSchema.json")
            .withStartupCheckStrategy(
                    new OneShotStartupCheckStrategy());
    @Container
    public static GenericContainer<?> migrateRowsCommandContainer = new GenericContainer<>(
            DockerImageName.parse("amazon/aws-cli:2.0.6"))
            .withEnv(Map.of("AWS_ACCESS_KEY_ID", "MOCK", "AWS_SECRET_ACCESS_KEY", "MOCK"))
            .withNetwork(containersNetwork)
            .withFileSystemBind(System.getProperty("user.dir") + "/src/test/resources/ProductSampleRows.json",
                    "/home/ProductSampleRows.json", BindMode.READ_ONLY)
            .withCommand(
                    "--endpoint-url=http://dynamodb:8000 --region=us-east-1 dynamodb batch-write-item  --request-items file:///home/ProductSampleRows.json")
            .withStartupCheckStrategy(
                    new OneShotStartupCheckStrategy());

    public static class TestProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            dynamodb.start();
            createTableCommandContainer.start();
            migrateRowsCommandContainer.start();
            String containerUrl = "http://" + dynamodb.getHost() + ":" + dynamodb.getFirstMappedPort();
            return new HashMap<String, String>() {
                {
                    put("quarkus.dynamodb.endpoint-override", containerUrl);
                    put("quarkus.dynamodb.aws.region", "us-east-1");
                    put("dynamodb.table-name.product", "products");
                }
            };

        }
    }

    @Test
    public void testGetAllProducts() {
        // asumiendo que la tabla esté creada y con datos
        Response response = RestAssured.get();
        JsonArray payload = new JsonArray(response.getBody().asString());
        Assertions.assertTrue(payload.size() > 0);
        JsonObject firstRecord = payload.getJsonObject(0);
        Assertions.assertNotNull(firstRecord.getString("id"));
        Assertions.assertNotNull(firstRecord.getString("name"));
        Assertions.assertNotNull(firstRecord.getString("description"));
        Assertions.assertNotNull(firstRecord.getString("brand"));
        Assertions.assertNotNull(firstRecord.getString("model"));
        Assertions.assertNotNull(firstRecord.getString("category"));
        Assertions.assertNotNull(firstRecord.getString("price"));
        Assertions.assertNotNull(firstRecord.getString("discount"));
    }

    @Test
    public void testNonExistantProductShouldReturn404() {
        RestAssured.given()
                .pathParam("id", "non-existant-id")
                .when().get("/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testGetProductById() {
        // asumiendo que la tabla esté creada y con datos, y que
        // "3187b908-62f8-4ce8-ad90-2fa4f475b899" existe
        Response response = RestAssured.get("/3187b908-62f8-4ce8-ad90-2fa4f475b899");
        JsonObject payload = new JsonObject(response.getBody().asString());
        Assertions.assertNotNull(payload.getString("id"));
        Assertions.assertNotNull(payload.getString("name"));
        Assertions.assertNotNull(payload.getString("description"));
        Assertions.assertNotNull(payload.getString("brand"));
        Assertions.assertNotNull(payload.getString("model"));
        Assertions.assertNotNull(payload.getString("category"));
        Assertions.assertNotNull(payload.getString("price"));
        Assertions.assertNotNull(payload.getString("discount"));
    }

    @Test
    public void insertNewRecord() {
        JsonObject payload = new JsonObject();
        payload.put("name", "Test Product");
        payload.put("description", "Test Description");
        payload.put("brand", "Test Brand");
        payload.put("model", "Test Model");
        payload.put("category", "Test Category");
        payload.put("price", 100);
        // asumiendo que la tabla esté creada y con datos
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(
                        payload.toString())
                .when().post();
        Assertions.assertEquals(201, response.getStatusCode());
        JsonObject generatedPayload = new JsonObject(response.getBody().asString());
        Assertions.assertNotNull(generatedPayload.getString("id"));
        Assertions.assertEquals(10, generatedPayload.getDouble("discount"));
    }

    @Test
    public void testIncompletePayloadShoudlReturnBadRequest() {
        JsonObject payload = new JsonObject();
        payload.put("name", "Test Product");
        // asumiendo que la tabla esté creada y con datos
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(
                        payload.toString())
                .when().post();
        Assertions.assertEquals(400, response.getStatusCode());
    }

    @Test
    public void invalidPriceShouldReturnBadRequest() {
        JsonObject payload = new JsonObject();
        payload.put("name", "Test Product");
        payload.put("description", "Test Description");
        payload.put("brand", "Test Brand");
        payload.put("model", "Test Model");
        payload.put("category", "Test Category");
        payload.put("price", -2);
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(
                        payload.toString())
                .when().post();
        Assertions.assertEquals(400, response.getStatusCode());
    }
}
