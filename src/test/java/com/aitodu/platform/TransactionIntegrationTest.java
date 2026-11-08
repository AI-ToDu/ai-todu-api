package com.aitodu.platform;

import static org.junit.jupiter.api.Assertions.*;

import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
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
class TransactionIntegrationTest {
  @LocalServerPort int port;
  @Autowired JdbcTemplate db;
  private final HttpClient client = HttpClient.newHttpClient();
  private final JsonMapper json = JsonMapper.builder().build();
  private String token;
  private UUID tenant;

  private HttpResponse<String> send(String method, String path, Object body, String key)
      throws Exception {
    var r =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/" + path))
            .header("Content-Type", "application/json");
    if (token != null) r.header("Authorization", "Bearer " + token);
    if (key != null) r.header("Idempotency-Key", key);
    return client.send(
        r.method(
                method,
                body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
            .build(),
        HttpResponse.BodyHandlers.ofString());
  }

  private JsonNode call(String m, String p, Object b, int expected) throws Exception {
    var r = send(m, p, b, null);
    assertEquals(expected, r.statusCode(), r.body());
    return r.body().isBlank() ? json.nullNode() : json.readTree(r.body());
  }

  private void workspace() throws Exception {
    token = null;
    String email = UUID.randomUUID() + "@example.test";
    call(
        "POST",
        "auth/register",
        Map.of(
            "name",
            "Test Admin",
            "company",
            "Fictional Workspace",
            "email",
            email,
            "password",
            "TestOnly2026!"),
        201);
    var login =
        call("POST", "auth/login", Map.of("email", email, "password", "TestOnly2026!"), 200);
    token = login.path("token").asText();
    assertEquals(3, token.split("\\.").length);
    tenant = UUID.fromString(login.path("user").path("tenantId").asText());
  }

  private String product(String sku, int quantity) throws Exception {
    String id =
        call(
                "POST",
                "products",
                Map.of("sku", sku, "name", sku, "price", 10, "cost", 6, "minimum", 0),
                201)
            .path("id")
            .asText();
    if (quantity > 0)
      call(
          "POST",
          "movements",
          Map.of(
              "productId", id, "quantity", quantity, "type", "RECEIPT", "reason", "Test receipt"),
          201);
    return id;
  }

  private Map<String, Object> sale(String product, int quantity) {
    return Map.of("lines", List.of(Map.of("productId", product, "quantity", quantity)));
  }

  private int stock(String id) throws Exception {
    for (var p : call("GET", "products", null, 200).path("items"))
      if (p.path("id").asText().equals(id)) return p.path("quantity").asInt();
    throw new AssertionError("Missing item");
  }

  @Test
  void failedMultiLineSaleRollsBackStockMovementsAndOutbox() throws Exception {
    workspace();
    String a = product("ROLL-A", 3), b = product("ROLL-B", 0);
    call(
        "POST",
        "sales",
        Map.of(
            "lines",
            List.of(Map.of("productId", a, "quantity", 2), Map.of("productId", b, "quantity", 1))),
        409);
    assertEquals(3, stock(a));
    assertEquals(0, call("GET", "sales", null, 200).path("items").size());
    assertEquals(1, call("GET", "movements", null, 200).path("items").size());
    assertEquals(
        0,
        db.queryForObject(
            "select count(*) from integration_outbox where tenant_id=?", Integer.class, tenant));
  }

  @Test
  void concurrentSalesCannotOversell() throws Exception {
    workspace();
    String p = product("LAST-UNIT", 1);
    var pool = Executors.newFixedThreadPool(6);
    try {
      var start = new CountDownLatch(1);
      var calls = new ArrayList<Future<Integer>>();
      for (int n = 0; n < 6; n++)
        calls.add(
            pool.submit(
                () -> {
                  start.await();
                  return send("POST", "sales", sale(p, 1), null).statusCode();
                }));
      start.countDown();
      int successes = 0;
      for (var f : calls) {
        int status = f.get(20, TimeUnit.SECONDS);
        assertTrue(status == 201 || status == 409, "Unexpected status " + status);
        if (status == 201) successes++;
      }
      assertEquals(1, successes);
      assertEquals(0, stock(p));
      assertEquals(1, call("GET", "sales", null, 200).path("items").size());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void idempotentSaleAndConflictingRetry() throws Exception {
    workspace();
    String p = product("IDEMPOTENT", 5), key = UUID.randomUUID().toString();
    var one = send("POST", "sales", sale(p, 2), key);
    var two = send("POST", "sales", sale(p, 2), key);
    assertEquals(201, one.statusCode());
    assertEquals(201, two.statusCode());
    assertEquals(json.readTree(one.body()).path("id"), json.readTree(two.body()).path("id"));
    assertEquals(3, stock(p));
    assertEquals(409, send("POST", "sales", sale(p, 1), key).statusCode());
  }

  @Test
  void partialReceiptsReturnsAndRoleBoundaries() throws Exception {
    workspace();
    String p = product("SUPPLY", 0);
    String supplier =
        call(
                "POST",
                "suppliers",
                Map.of("name", "Test Supplier", "contact", "Fictional contact"),
                201)
            .path("id")
            .asText();
    var order =
        call(
            "POST",
            "purchases",
            Map.of("supplierId", supplier, "lines", List.of(Map.of("productId", p, "quantity", 5))),
            201);
    String path = "purchases/" + order.path("id").asText() + "/receipts",
        line = order.path("lines").get(0).path("id").asText();
    assertEquals(
        "PARTIAL",
        call("POST", path, Map.of("lines", List.of(Map.of("lineId", line, "quantity", 3))), 201)
            .path("status")
            .asText());
    call("POST", path, Map.of("lines", List.of(Map.of("lineId", line, "quantity", 3))), 409);
    assertEquals(3, stock(p));
    var s = call("POST", "sales", sale(p, 2), 201);
    String saleId = s.path("id").asText(), saleLine = s.path("lines").get(0).path("id").asText();
    call(
        "POST",
        "returns",
        Map.of(
            "saleId",
            saleId,
            "saleLineId",
            saleLine,
            "quantity",
            1,
            "disposition",
            "RESTOCK",
            "reason",
            "Test restock"),
        201);
    assertEquals(2, stock(p));
    call(
        "POST",
        "returns",
        Map.of(
            "saleId",
            saleId,
            "saleLineId",
            saleLine,
            "quantity",
            2,
            "disposition",
            "LOSS",
            "reason",
            "Excess"),
        409);
    assertEquals(2, stock(p));
    String email = UUID.randomUUID() + "@example.test";
    call(
        "POST",
        "employees",
        Map.of(
            "name",
            "Warehouse Test",
            "email",
            email,
            "role",
            "WAREHOUSE",
            "password",
            "TestOnly2026!"),
        201);
    token =
        call("POST", "auth/login", Map.of("email", email, "password", "TestOnly2026!"), 200)
            .path("token")
            .asText();
    call("POST", "sales", sale(p, 1), 403);
    call("GET", "employees", null, 403);
    call("GET", "movements", null, 200);
    call("GET", "products", null, 200);
  }
}
