package com.aitodu.platform;

import static org.junit.jupiter.api.Assertions.*;

import com.aitodu.platform.logistics.domain.services.WeatherForecastGateway;
import com.aitodu.platform.shared.infrastructure.outbox.OutboxRelay;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Exercises each documented operation with synthetic data and records sanitized HTTP evidence. */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.datasource.url=${TEST_DATABASE_URL:jdbc:h2:mem:contracts;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1}",
      "spring.datasource.username=${TEST_DATABASE_USER:sa}",
      "spring.datasource.password=${TEST_DATABASE_PASSWORD:}",
      "app.outbox.initial-delay-ms=86400000"
    })
@Import(ServicesContractIntegrationTest.WeatherFixture.class)
class ServicesContractIntegrationTest {
  @LocalServerPort int port;
  @Autowired OutboxRelay relay;
  private final JsonMapper json = JsonMapper.builder().build();
  private final HttpClient client = HttpClient.newHttpClient();
  private final List<Map<String, Object>> evidence = new ArrayList<>();
  private String token;

  @TestConfiguration
  static class WeatherFixture {
    @Bean
    @Primary
    WeatherForecastGateway fixtureWeather() {
      return () ->
          new WeatherForecastGateway.Forecast(
              "Lima fixture",
              21,
              0,
              8,
              Instant.parse("2026-11-08T12:00:00Z"),
              Instant.parse("2026-11-08T12:00:00Z"));
    }
  }

  private JsonNode call(String method, String path, String body, int expected) throws Exception {
    var request =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
            .header("Content-Type", "application/json")
            .header("Accept-Language", "en-US");
    if (token != null) request.header("Authorization", "Bearer " + token);
    request.method(
        method,
        body == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(body));
    var response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    assertEquals(expected, response.statusCode(), method + " " + path + " " + response.body());
    var result = response.body().isBlank() ? json.nullNode() : json.readTree(response.body());
    if (expected >= 400) {
      assertTrue(
          response
              .headers()
              .firstValue("Content-Type")
              .orElse("")
              .contains("application/problem+json"));
      assertEquals(expected, result.path("status").asInt(), response.body());
      assertFalse(result.path("code").asText().isBlank());
    }
    evidence.add(
        Map.of(
            "method",
            method,
            "path",
            path,
            "status",
            expected,
            "request",
            redact(body == null ? "" : body),
            "response",
            redact(response.body())));
    return result;
  }

  private String redact(String value) {
    return value.replaceAll("(\"(?:token|password)\"\\s*:\\s*)\"[^\"]*\"", "$1\"[REDACTED]\"");
  }

  private JsonNode api(String method, String path, String body, int status) throws Exception {
    return call(method, "/api/v1/" + path, body, status);
  }

  @Test
  void allOperationsMatchTheirPublishedContractAndRejectInvalidInput() throws Exception {
    String email = "contracts-" + UUID.randomUUID() + "@example.test";
    api(
        "POST",
        "auth/register",
        "{\"name\":\"Contract review\",\"company\":\"Synthetic company\",\"email\":\""
            + email
            + "\",\"password\":\"ExampleOnly2026!\"}",
        201);
    token =
        api(
                "POST",
                "auth/login",
                "{\"email\":\"" + email + "\",\"password\":\"ExampleOnly2026!\"}",
                200)
            .get("token")
            .asText();
    api("GET", "auth/me", null, 200);
    String employeeEmail = "employee-" + UUID.randomUUID() + "@example.test";
    api(
        "POST",
        "employees",
        "{\"name\":\"Operator\",\"email\":\""
            + employeeEmail
            + "\",\"role\":\"WAREHOUSE\",\"password\":\"ExampleOnly2026!\"}",
        201);
    api("GET", "employees", null, 200);
    var product =
        api(
            "POST",
            "products",
            "{\"sku\":\"CONTRACT-001\",\"name\":\"Example"
                + " item\",\"price\":12.50,\"cost\":7,\"minimum\":1}",
            201);
    String id = product.get("id").asText();
    api("GET", "products", null, 200);
    api("PATCH", "products/" + id, "{\"conservation\":\"GOOD\"}", 200);
    api(
        "POST",
        "movements",
        "{\"productId\":\""
            + id
            + "\",\"quantity\":5,\"type\":\"RECEIPT\",\"reason\":\"Initial receipt\"}",
        201);
    api("GET", "movements", null, 200);
    String supplier =
        api(
                "POST",
                "suppliers",
                "{\"name\":\"Example supplier\",\"contact\":\"supplier@example.test\"}",
                201)
            .get("id")
            .asText();
    api("GET", "suppliers", null, 200);
    var purchase =
        api(
            "POST",
            "purchases",
            "{\"supplierId\":\""
                + supplier
                + "\",\"lines\":[{\"productId\":\""
                + id
                + "\",\"quantity\":3}]}",
            201);
    api("GET", "purchases", null, 200);
    api(
        "POST",
        "purchases/" + purchase.get("id").asText() + "/receipts",
        "{\"lines\":[{\"lineId\":\""
            + purchase.get("lines").get(0).get("id").asText()
            + "\",\"quantity\":2}]}",
        201);
    var sale =
        api("POST", "sales", "{\"lines\":[{\"productId\":\"" + id + "\",\"quantity\":2}]}", 201);
    api("GET", "sales", null, 200);
    String driver =
        api("POST", "drivers", "{\"name\":\"Example driver\",\"license\":\"LIC-CONTRACT\"}", 201)
            .get("id")
            .asText();
    String vehicle =
        api("POST", "vehicles", "{\"name\":\"Example van\",\"plate\":\"CON-001\"}", 201)
            .get("id")
            .asText();
    api("GET", "drivers", null, 200);
    api("GET", "vehicles", null, 200);
    api("GET", "dispatch-candidates", null, 200);
    String dispatch =
        api(
                "POST",
                "dispatches",
                "{\"saleIds\":[\""
                    + sale.get("id").asText()
                    + "\"],\"driverId\":\""
                    + driver
                    + "\",\"vehicleId\":\""
                    + vehicle
                    + "\",\"destination\":\"Example Lima warehouse\"}",
                201)
            .get("id")
            .asText();
    api("GET", "dispatches", null, 200);
    api(
        "POST",
        "dispatches/" + dispatch + "/incidents",
        "{\"description\":\"Recipient contacted before delivery\"}",
        201);
    api("PATCH", "dispatches/" + dispatch, "{\"status\":\"IN_TRANSIT\"}", 200);
    api(
        "PATCH",
        "dispatches/" + dispatch,
        "{\"status\":\"DELIVERED\",\"recipient\":\"Example recipient\"}",
        200);
    api(
        "POST",
        "returns",
        "{\"saleId\":\""
            + sale.get("id").asText()
            + "\",\"saleLineId\":\""
            + sale.get("lines").get(0).get("id").asText()
            + "\",\"quantity\":1,\"disposition\":\"RESTOCK\",\"reason\":\"Example return\"}",
        201);
    api("GET", "returns", null, 200);
    var cancellation =
        api("POST", "sales", "{\"lines\":[{\"productId\":\"" + id + "\",\"quantity\":1}]}", 201);
    api(
        "POST",
        "sales/" + cancellation.get("id").asText() + "/cancel",
        "{\"reason\":\"Example cancellation\"}",
        200);
    int remaining = api("GET", "products", null, 200).get("items").get(0).get("quantity").asInt();
    if (remaining > 0)
      api(
          "POST",
          "movements",
          "{\"productId\":\""
              + id
              + "\",\"quantity\":"
              + remaining
              + ",\"type\":\"ISSUE\",\"reason\":\"Alert reference review\"}",
          201);
    relay.relay();
    var notifications = api("GET", "notifications", null, 200);
    assertTrue(notifications.get("items").size() > 0);
    boolean productIdentified = false;
    for (var notification : notifications.get("items"))
      if (notification.get("type").asText().equals("LOW_STOCK")) {
        assertEquals(id, notification.get("productId").asText());
        productIdentified = true;
      }
    assertTrue(
        productIdentified, "The HTTP notification resource must identify the low-stock product");
    api(
        "PATCH",
        "notifications/" + notifications.get("items").get(0).get("id").asText(),
        null,
        200);
    api("GET", "metrics", null, 200);
    api("GET", "logistics/weather", null, 200);
    api("POST", "products", "{}", 400);
    api("POST", "products", "{", 400);
    api("POST", "sales", "{\"lines\":[null]}", 400);
    api("PATCH", "products/not-a-uuid", "{\"conservation\":\"GOOD\"}", 400);
    api("POST", "sales", "{\"lines\":[{\"productId\":\"" + id + "\",\"quantity\":999}]}", 409);
    api(
        "POST",
        "movements",
        "{\"productId\":\""
            + id
            + "\",\"quantity\":1.5,\"type\":\"RECEIPT\",\"reason\":\"Fractional units\"}",
        422);
    String adminToken = token;
    token =
        api(
                "POST",
                "auth/login",
                "{\"email\":\"" + employeeEmail + "\",\"password\":\"ExampleOnly2026!\"}",
                200)
            .get("token")
            .asText();
    api("GET", "metrics", null, 403);
    token = adminToken;
    api("POST", "auth/logout", "{}", 204);
    api("GET", "products", null, 401);
    token = null;
    var document = call("GET", "/v3/api-docs", null, 200);
    int count = 0;
    for (var path : document.get("paths").properties()) {
      for (var operation : path.getValue().properties()) {
        if (!Set.of("get", "post", "patch", "put", "delete").contains(operation.getKey())) continue;
        count++;
        assertFalse(operation.getValue().path("summary").asText().isBlank());
        assertTrue(operation.getValue().path("tags").size() > 0);
      }
    }
    assertEquals(34, count);
    for (var entry : evidence) {
      String path = (String) entry.get("path");
      if (!path.startsWith("/api/v1/")) continue;
      path =
          path.replaceAll("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "{id}")
              .replace("not-a-uuid", "{id}");
      var operation =
          document.get("paths").get(path).get(((String) entry.get("method")).toLowerCase());
      assertNotNull(
          operation.get("responses").get(String.valueOf(entry.get("status"))),
          path + " " + entry.get("status"));
    }
    assertEquals(0, document.at("/paths/~1api~1v1~1auth~1login/post/security").size());
    assertEquals(
        "#/components/schemas/ProductResource",
        document
            .at(
                "/paths/~1api~1v1~1products/post/responses/201/content/application~1json/schema/$ref")
            .asText());
    Files.createDirectories(Path.of("target/contract-evidence"));
    evidence.removeIf(e -> e.get("path").equals("/v3/api-docs"));
    Files.writeString(
        Path.of("target/contract-evidence/http.json"),
        json.writerWithDefaultPrettyPrinter().writeValueAsString(evidence));
    Files.writeString(
        Path.of("target/contract-evidence/openapi.json"),
        json.writerWithDefaultPrettyPrinter().writeValueAsString(document));
  }
}
