package com.aitodu.platform.shared.infrastructure.documentation;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.*;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import java.util.*;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.*;
import tools.jackson.databind.json.JsonMapper;

/** Explicit HTTP contracts for the Open Host Service, including command error representations. */
@Configuration
public class EndpointDocumentation {
  private record Endpoint(
      String method,
      String path,
      String tag,
      String summary,
      int status,
      String response,
      String roles,
      String description) {}

  private static final List<Endpoint> ENDPOINTS =
      List.of(
          new Endpoint(
              "POST",
              "/api/v1/auth/register",
              "IAM",
              "Register a workspace",
              201,
              "UserResource",
              "Public",
              "Create a company and its first administrator; email must be unique."),
          new Endpoint(
              "POST",
              "/api/v1/auth/login",
              "IAM",
              "Create a session",
              200,
              "SessionResource",
              "Public",
              "Authenticate an active account and return a revocable JWT."),
          new Endpoint(
              "GET",
              "/api/v1/auth/me",
              "IAM",
              "Read the current identity",
              200,
              "UserResource",
              "Any active role",
              "Read the authenticated identity; tenant is derived from the token."),
          new Endpoint(
              "POST",
              "/api/v1/auth/logout",
              "IAM",
              "Revoke the current session",
              204,
              null,
              "Any active role",
              "Revoke the presented token; later access with it returns 401."),
          new Endpoint(
              "GET",
              "/api/v1/employees",
              "IAM",
              "List employees",
              200,
              null,
              "ADMIN",
              "List employees in the authenticated workspace."),
          new Endpoint(
              "POST",
              "/api/v1/employees",
              "IAM",
              "Create an employee",
              201,
              "EmployeeResource",
              "ADMIN",
              "Provision a role and initial password; no password or hash is returned."),
          new Endpoint(
              "GET",
              "/api/v1/products",
              "Inventory",
              "List products",
              200,
              null,
              "Any active role",
              "List products in the authenticated workspace; no server-side pagination or"
                  + " filters."),
          new Endpoint(
              "POST",
              "/api/v1/products",
              "Inventory",
              "Register a product",
              201,
              "ProductResource",
              "ADMIN, WAREHOUSE",
              "Register a unique workspace SKU with zero initial stock. Amounts are PEN."),
          new Endpoint(
              "PATCH",
              "/api/v1/products/{id}",
              "Inventory",
              "Change conservation status",
              200,
              "ProductResource",
              "ADMIN, WAREHOUSE",
              "Change conservation; this action does not edit price or stock."),
          new Endpoint(
              "GET",
              "/api/v1/movements",
              "Inventory",
              "List stock movements",
              200,
              null,
              "ADMIN, WAREHOUSE",
              "Read the audit history of signed stock movements."),
          new Endpoint(
              "POST",
              "/api/v1/movements",
              "Inventory",
              "Record a stock movement",
              201,
              "ProductResource",
              "ADMIN, WAREHOUSE",
              "Apply RECEIPT or ISSUE in whole positive units; an issue cannot exceed available"
                  + " stock."),
          new Endpoint(
              "GET",
              "/api/v1/suppliers",
              "Procurement",
              "List suppliers",
              200,
              null,
              "ADMIN, WAREHOUSE",
              "Read suppliers belonging to this workspace."),
          new Endpoint(
              "POST",
              "/api/v1/suppliers",
              "Procurement",
              "Register a supplier",
              201,
              "SupplierResource",
              "ADMIN, WAREHOUSE",
              "Register supplier name and contact information."),
          new Endpoint(
              "GET",
              "/api/v1/purchases",
              "Procurement",
              "List purchase orders",
              200,
              null,
              "ADMIN, WAREHOUSE",
              "Read orders and ordered/received quantities."),
          new Endpoint(
              "POST",
              "/api/v1/purchases",
              "Procurement",
              "Place a purchase order",
              201,
              "PurchaseResource",
              "ADMIN, WAREHOUSE",
              "Create an order for an existing supplier and distinct workspace products."),
          new Endpoint(
              "POST",
              "/api/v1/purchases/{id}/receipts",
              "Procurement",
              "Receive a purchase partially or fully",
              201,
              "PurchaseResource",
              "ADMIN, WAREHOUSE",
              "Receive positive quantities by order line; exceeding outstanding units rolls back"
                  + " the entire receipt."),
          new Endpoint(
              "GET",
              "/api/v1/sales",
              "Sales",
              "List sales",
              200,
              null,
              "ADMIN, SALES",
              "Read sales, immutable commercial line snapshots and academic receipt references."),
          new Endpoint(
              "POST",
              "/api/v1/sales",
              "Sales",
              "Confirm a sale",
              201,
              "SaleResource",
              "ADMIN, SALES",
              "Atomically record sale, stock issues and integration messages. An identical"
                  + " Idempotency-Key replay returns the existing sale with 201; a conflicting"
                  + " payload returns 409."),
          new Endpoint(
              "POST",
              "/api/v1/sales/{id}/cancel",
              "Sales",
              "Cancel a sale",
              200,
              "SaleResource",
              "ADMIN, SALES",
              "Cancel a sale that has not been dispatched and restore its stock once."),
          new Endpoint(
              "GET",
              "/api/v1/returns",
              "Returns",
              "List customer returns",
              200,
              null,
              "ADMIN, SALES",
              "Read previously recorded returns and disposition."),
          new Endpoint(
              "POST",
              "/api/v1/returns",
              "Returns",
              "Record a customer return",
              201,
              "ReturnResource",
              "ADMIN, SALES",
              "Return units against one sale line. RESTOCK restores stock; LOSS records loss."
                  + " Previously returned quantities limit the remaining return."),
          new Endpoint(
              "GET",
              "/api/v1/dispatches",
              "Logistics",
              "List dispatch manifests",
              200,
              null,
              "ADMIN, LOGISTICS",
              "Read manifests, lines, incidents and recipient evidence."),
          new Endpoint(
              "POST",
              "/api/v1/dispatches",
              "Logistics",
              "Prepare a dispatch",
              201,
              "DispatchResource",
              "ADMIN, LOGISTICS",
              "Assign confirmed undispatched sales, a driver, a vehicle and destination. Stock is"
                  + " not deducted again."),
          new Endpoint(
              "PATCH",
              "/api/v1/dispatches/{id}",
              "Logistics",
              "Transition a dispatch",
              200,
              "DispatchResource",
              "ADMIN, LOGISTICS",
              "Transition PENDING to IN_TRANSIT, then DELIVERED with recipient evidence; invalid"
                  + " transitions preserve the previous state."),
          new Endpoint(
              "POST",
              "/api/v1/dispatches/{id}/incidents",
              "Logistics",
              "Record a delivery incident",
              201,
              "DispatchResource",
              "ADMIN, LOGISTICS",
              "Append an incident description to the selected manifest."),
          new Endpoint(
              "GET",
              "/api/v1/dispatch-candidates",
              "Logistics",
              "List eligible sales",
              200,
              null,
              "ADMIN, LOGISTICS",
              "Read confirmed sales that can be assigned to a dispatch."),
          new Endpoint(
              "GET",
              "/api/v1/drivers",
              "Logistics",
              "List drivers",
              200,
              null,
              "ADMIN, LOGISTICS",
              "Read drivers registered in this workspace."),
          new Endpoint(
              "POST",
              "/api/v1/drivers",
              "Logistics",
              "Register a driver",
              201,
              "DriverResponseResource",
              "ADMIN, LOGISTICS",
              "Register a driver and unique license."),
          new Endpoint(
              "GET",
              "/api/v1/vehicles",
              "Logistics",
              "List vehicles",
              200,
              null,
              "ADMIN, LOGISTICS",
              "Read vehicles registered in this workspace."),
          new Endpoint(
              "POST",
              "/api/v1/vehicles",
              "Logistics",
              "Register a vehicle",
              201,
              "VehicleResponseResource",
              "ADMIN, LOGISTICS",
              "Register a vehicle and unique plate."),
          new Endpoint(
              "GET",
              "/api/v1/logistics/weather",
              "Logistics",
              "Read regional Lima weather",
              200,
              "WeatherResource",
              "ADMIN, LOGISTICS",
              "Read fixed central-Lima conditions from Open-Meteo, cached for ten minutes. Provider"
                  + " failure returns 503; dispatch commands remain usable."),
          new Endpoint(
              "GET",
              "/api/v1/notifications",
              "Notifications",
              "List notifications",
              200,
              null,
              "Any active role",
              "Read workspace notifications produced by committed integration messages."),
          new Endpoint(
              "PATCH",
              "/api/v1/notifications/{id}",
              "Notifications",
              "Mark a notification as read",
              200,
              "NotificationResource",
              "Any active role",
              "Mark a workspace notification as read; repeating it preserves the read state."),
          new Endpoint(
              "GET",
              "/api/v1/metrics",
              "Reporting",
              "Read operational metrics",
              200,
              "Metrics",
              "ADMIN",
              "Read revenue, gross contribution, inventory valuation, low-stock count and"
                  + " dispatch/sale counts. Gross contribution is not net profit."));
  private static final Map<String, String> EXAMPLES =
      Map.ofEntries(
          Map.entry(
              "RegistrationResource",
              "{\"name\": \"AV2 Reviewer\", \"company\": \"Example workspace\", \"email\":"
                  + " \"reviewer@example.test\", \"password\": \"ExampleOnly2026!\"}"),
          Map.entry(
              "LoginResource",
              "{\"email\": \"reviewer@example.test\", \"password\": \"ExampleOnly2026!\"}"),
          Map.entry(
              "CreateEmployeeResource",
              "{\"name\": \"Warehouse operator\", \"email\": \"warehouse@example.test\", \"role\":"
                  + " \"WAREHOUSE\", \"password\": \"ExampleOnly2026!\"}"),
          Map.entry(
              "CreateProductResource",
              "{\"sku\": \"AV2-DEMO-001\", \"name\": \"Example item\", \"price\": 12.5, \"cost\":"
                  + " 7, \"minimum\": 1}"),
          Map.entry(
              "MovementResource",
              "{\"productId\": \"00000000-0000-4000-8000-000000000001\", \"quantity\": 5, \"type\":"
                  + " \"RECEIPT\", \"reason\": \"Initial receipt\"}"),
          Map.entry("ConservationResource", "{\"conservation\": \"GOOD\"}"),
          Map.entry(
              "CreateSupplierResource",
              "{\"name\": \"Example supplier\", \"contact\": \"supplier@example.test\"}"),
          Map.entry(
              "PlacePurchaseResource",
              "{\"supplierId\": \"00000000-0000-4000-8000-000000000001\", \"lines\":"
                  + " [{\"productId\": \"00000000-0000-4000-8000-000000000001\", \"quantity\":"
                  + " 5}]}"),
          Map.entry(
              "ReceivePurchaseResource",
              "{\"lines\": [{\"lineId\": \"00000000-0000-4000-8000-000000000001\", \"quantity\":"
                  + " 2}]}"),
          Map.entry(
              "ConfirmSaleResource",
              "{\"lines\": [{\"productId\": \"00000000-0000-4000-8000-000000000001\", \"quantity\":"
                  + " 1}]}"),
          Map.entry("CancelResource", "{\"reason\": \"Customer cancellation\"}"),
          Map.entry(
              "RecordReturnResource",
              "{\"saleId\": \"00000000-0000-4000-8000-000000000001\", \"saleLineId\":"
                  + " \"00000000-0000-4000-8000-000000000001\", \"quantity\": 1, \"disposition\":"
                  + " \"RESTOCK\", \"reason\": \"Customer return\"}"),
          Map.entry(
              "PrepareDispatchResource",
              "{\"saleIds\": [\"00000000-0000-4000-8000-000000000001\"], \"driverId\":"
                  + " \"00000000-0000-4000-8000-000000000001\", \"vehicleId\":"
                  + " \"00000000-0000-4000-8000-000000000001\", \"destination\": \"Example"
                  + " warehouse, Lima\"}"),
          Map.entry(
              "TransitionResource",
              "{\"status\": \"DELIVERED\", \"recipient\": \"Example recipient\"}"),
          Map.entry("IncidentResource", "{\"description\": \"Recipient temporarily unavailable\"}"),
          Map.entry(
              "DriverResource", "{\"name\": \"Example driver\", \"license\": \"LIC-AV2-001\"}"),
          Map.entry("VehicleResource", "{\"name\": \"Example van\", \"plate\": \"AV2-001\"}"));

  @Bean
  OpenApiCustomizer serviceContracts() {
    return api -> {
      var problem =
          new Schema<>()
              .type("object")
              .required(List.of("type", "title", "status", "detail", "code"));
      problem.addProperty("type", new StringSchema().example("about:blank"));
      problem.addProperty("title", new StringSchema().example("Conflict"));
      problem.addProperty("status", new IntegerSchema().example(409));
      problem.addProperty("detail", new StringSchema().example("There is not enough stock."));
      problem.addProperty("code", new StringSchema().example("INSUFFICIENT_STOCK"));
      api.getComponents().addSchemas("ProblemDetail", problem);
      var json = JsonMapper.builder().build();
      for (String name :
          List.of(
              "MovementResource",
              "LineRequest",
              "RequestedPurchaseLineResource",
              "ReceivedPurchaseLineResource",
              "RecordReturnResource")) {
        var schema = api.getComponents().getSchemas().get(name);
        if (schema != null
            && schema.getProperties() != null
            && schema.getProperties().containsKey("quantity")) {
          var quantity = (Schema<?>) schema.getProperties().get("quantity");
          quantity.setMultipleOf(java.math.BigDecimal.ONE);
          quantity.setMaximum(java.math.BigDecimal.valueOf(Integer.MAX_VALUE));
          quantity.setDescription("Positive whole units; fractions are rejected by the domain.");
        }
      }
      var product = api.getComponents().getSchemas().get("CreateProductResource");
      if (product != null)
        for (String field : List.of("price", "cost")) {
          var amount = (Schema<?>) product.getProperties().get(field);
          amount.setMultipleOf(new java.math.BigDecimal("0.01"));
          amount.setDescription("Non-negative PEN amount with at most two decimal places.");
        }
      enumeration(
          api, "ConservationResource", "conservation", List.of("GOOD", "DAMAGED", "QUARANTINED"));
      enumeration(api, "MovementResource", "type", List.of("RECEIPT", "ISSUE", "LOSS"));
      enumeration(api, "RecordReturnResource", "disposition", List.of("RESTOCK", "LOSS"));
      enumeration(api, "TransitionResource", "status", List.of("IN_TRANSIT", "DELIVERED"));
      enumeration(
          api,
          "CreateEmployeeResource",
          "role",
          List.of("ADMIN", "WAREHOUSE", "SALES", "LOGISTICS"));
      for (var e : ENDPOINTS) {
        var item = api.getPaths().get(e.path());
        if (item == null) throw new IllegalStateException("Undocumented route " + e.path());
        var op = item.readOperationsMap().get(PathItem.HttpMethod.valueOf(e.method()));
        op.setTags(List.of(e.tag()));
        op.setSummary(e.summary());
        op.setDescription(
            e.description()
                + " Allowed roles: "
                + e.roles()
                + ". Tenant is never accepted from request input.");
        op.setSecurity(
            e.roles().equals("Public")
                ? List.of()
                : List.of(new SecurityRequirement().addList("bearerAuth")));
        var responses = new ApiResponses();
        var success =
            new ApiResponse()
                .description(
                    e.status() == 201
                        ? "Created"
                        : e.status() == 204
                            ? "Session revoked; no response body"
                            : "Successful operation");
        if (e.response() != null)
          success.content(
              new Content()
                  .addMediaType(
                      "application/json",
                      new MediaType()
                          .schema(new Schema<>().$ref("#/components/schemas/" + e.response()))));
        else if (e.status() != 204 && op.getResponses().get("200") != null) {
          var original = op.getResponses().get("200").getContent();
          if (original != null && !original.isEmpty())
            success.setContent(
                new Content()
                    .addMediaType("application/json", original.values().iterator().next()));
        }
        responses.addApiResponse(String.valueOf(e.status()), success);
        if (!e.roles().equals("Public")) {
          error(
              responses, "401", "Missing, invalid, expired or revoked session", "UNAUTHENTICATED");
          if (!e.roles().equals("Any active role"))
            error(responses, "403", "Role does not permit this operation", "FORBIDDEN");
        } else if (e.path().endsWith("login"))
          error(responses, "401", "Invalid credentials or inactive account", "INVALID_CREDENTIALS");
        if (!e.method().equals("GET") && e.status() != 204) {
          error(
              responses,
              "400",
              "Malformed request, missing required input or invalid parameter",
              "INVALID_TEXT");
          error(
              responses,
              "422",
              "A domain invariant rejects the supplied values",
              "INVALID_QUANTITY");
          if (e.method().equals("POST"))
            error(responses, "409", "Duplicate identity or conflicting business state", "CONFLICT");
        }
        if (e.path().contains("{id}")
            || Set.of(
                        "/api/v1/sales",
                        "/api/v1/returns",
                        "/api/v1/purchases",
                        "/api/v1/dispatches",
                        "/api/v1/movements")
                    .contains(e.path())
                && e.method().equals("POST"))
          error(
              responses, "404", "Reference not found in the authenticated workspace", "NOT_FOUND");
        if (e.path().endsWith("weather"))
          error(responses, "503", "Weather provider unavailable", "UPSTREAM_UNAVAILABLE");
        error(
            responses,
            "500",
            "Unexpected server failure; internal details are not disclosed",
            "INTERNAL_ERROR");
        op.setResponses(responses);
        if (e.path().contains("{id}")) {
          if (op.getParameters() != null)
            op.getParameters().stream()
                .filter(p -> "id".equals(p.getName()))
                .forEach(
                    p -> p.setDescription("UUID of a resource in the authenticated workspace"));
        }
        op.addParametersItem(
            new Parameter()
                .in("header")
                .name("Accept-Language")
                .required(false)
                .description(
                    "Localized messages; en-US or es-419. Does not translate user-entered data.")
                .schema(new StringSchema()._enum(List.of("en-US", "es-419"))._default("en-US")));
        if (op.getRequestBody() != null) {
          var media = op.getRequestBody().getContent().get("application/json");
          if (media != null && media.getSchema() != null && media.getSchema().get$ref() != null) {
            String name =
                media
                    .getSchema()
                    .get$ref()
                    .substring(media.getSchema().get$ref().lastIndexOf('/') + 1);
            if (EXAMPLES.containsKey(name))
              media.setExample(json.readValue(EXAMPLES.get(name), Map.class));
          }
        }
      }
      api.setServers(
          List.of(
              new io.swagger.v3.oas.models.servers.Server()
                  .url("/")
                  .description("Current server")));
    };
  }

  private static void enumeration(OpenAPI api, String resource, String field, List<String> values) {
    var owner = api.getComponents().getSchemas().get(resource);
    if (owner != null) owner.addProperty(field, new StringSchema()._enum(values));
  }

  private static void error(
      ApiResponses responses, String status, String description, String code) {
    responses.addApiResponse(
        status,
        new ApiResponse()
            .description(description)
            .content(
                new Content()
                    .addMediaType(
                        "application/problem+json",
                        new MediaType()
                            .schema(new Schema<>().$ref("#/components/schemas/ProblemDetail"))
                            .example(
                                Map.of(
                                    "type",
                                    "about:blank",
                                    "title",
                                    description,
                                    "status",
                                    Integer.parseInt(status),
                                    "detail",
                                    description,
                                    "code",
                                    code)))));
  }
}
