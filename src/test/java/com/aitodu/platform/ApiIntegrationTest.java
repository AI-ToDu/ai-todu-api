package com.aitodu.platform;

import static org.junit.jupiter.api.Assertions.*;

import java.net.*;
import java.net.http.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.datasource.url=${TEST_DATABASE_URL:jdbc:h2:mem:api;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1}",
      "spring.datasource.username=${TEST_DATABASE_USER:sa}",
      "spring.datasource.password=${TEST_DATABASE_PASSWORD:}",
      "app.outbox.enabled=false"
    })
class ApiIntegrationTest {
  @LocalServerPort int port;
  private final HttpClient client = HttpClient.newHttpClient();
  private final JsonMapper json = JsonMapper.builder().build();
  private String token;

  private JsonNode call(String method, String path, String body, int status) throws Exception {
    var request =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/" + path))
            .header("Content-Type", "application/json");
    if (token != null) request.header("Authorization", "Bearer " + token);
    request.method(
        method,
        body == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(body));
    var response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    assertEquals(status, response.statusCode(), response.body());
    return response.body().isBlank() ? json.nullNode() : json.readTree(response.body());
  }

  @Test
  void completeWorkflowAndTenantIsolation() throws Exception {
    String email = "api-" + java.util.UUID.randomUUID() + "@example.test";
    call(
        "POST",
        "auth/register",
        "{\"name\":\"Operator\",\"company\":\"Test\",\"email\":\""
            + email
            + "\",\"password\":\"TestOnly2026!\"}",
        201);
    token =
        call(
                "POST",
                "auth/login",
                "{\"email\":\"" + email + "\",\"password\":\"TestOnly2026!\"}",
                200)
            .get("token")
            .asText();
    String product =
        call(
                "POST",
                "products",
                "{\"sku\":\"RICE-01\",\"name\":\"Rice\",\"price\":10.50,\"cost\":6,\"minimum\":2}",
                201)
            .get("id")
            .asText();
    call(
        "POST",
        "movements",
        "{\"productId\":\""
            + product
            + "\",\"quantity\":5,\"type\":\"RECEIPT\",\"reason\":\"Initial receipt\"}",
        201);
    call("POST", "sales", "{\"lines\":[{\"productId\":\"" + product + "\",\"quantity\":6}]}", 409);
    var sale =
        call(
            "POST",
            "sales",
            "{\"lines\":[{\"productId\":\"" + product + "\",\"quantity\":2}]}",
            201);
    assertEquals(21.0, sale.get("total").asDouble());
    String driver =
        call("POST", "drivers", "{\"name\":\"Driver\",\"license\":\"L-01\"}", 201)
            .get("id")
            .asText();
    String vehicle =
        call("POST", "vehicles", "{\"name\":\"Van\",\"plate\":\"ABC-123\"}", 201)
            .get("id")
            .asText();
    String dispatch =
        call(
                "POST",
                "dispatches",
                "{\"saleIds\":[\""
                    + sale.get("id").asText()
                    + "\"],\"driverId\":\""
                    + driver
                    + "\",\"vehicleId\":\""
                    + vehicle
                    + "\",\"destination\":\"Test warehouse Lima\"}",
                201)
            .get("id")
            .asText();
    call(
        "PATCH",
        "dispatches/" + dispatch,
        "{\"status\":\"DELIVERED\",\"recipient\":\"Test Recipient\"}",
        422);
    call("PATCH", "dispatches/" + dispatch, "{\"status\":\"IN_TRANSIT\"}", 200);
    call(
        "PATCH",
        "dispatches/" + dispatch,
        "{\"status\":\"DELIVERED\",\"recipient\":\"Test Recipient\"}",
        200);
    assertEquals(3, call("GET", "products", null, 200).get("items").get(0).get("quantity").asInt());
    call("POST", "auth/logout", "{}", 204);
    call("GET", "products", null, 401);
    token = null;
    String other = "other-" + java.util.UUID.randomUUID() + "@example.test";
    call(
        "POST",
        "auth/register",
        "{\"name\":\"Other\",\"company\":\"Other\",\"email\":\""
            + other
            + "\",\"password\":\"TestOnly2026!\"}",
        201);
    token =
        call(
                "POST",
                "auth/login",
                "{\"email\":\"" + other + "\",\"password\":\"TestOnly2026!\"}",
                200)
            .get("token")
            .asText();
    assertEquals(0, call("GET", "products", null, 200).get("items").size());
    call("POST", "sales", "{\"lines\":[{\"productId\":\"" + product + "\",\"quantity\":1}]}", 404);
  }
}
