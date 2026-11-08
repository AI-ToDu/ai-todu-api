package com.aitodu.platform;

import static org.junit.jupiter.api.Assertions.*;

import com.aitodu.platform.logistics.infrastructure.external.OpenMeteoWeatherGateway;
import com.aitodu.platform.shared.domain.DomainViolation;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class WeatherGatewayTest {
  @Test
  void translatesProviderContractAndCachesRegionalForecast() throws Exception {
    var requests = new AtomicInteger();
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/forecast",
        e -> {
          requests.incrementAndGet();
          var data =
              "{\"current\":{\"time\":\"2026-11-07T20:00\",\"temperature_2m\":21.5,\"precipitation\":0.2,\"wind_speed_10m\":12}}"
                  .getBytes(StandardCharsets.UTF_8);
          e.sendResponseHeaders(200, data.length);
          e.getResponseBody().write(data);
          e.close();
        });
    server.start();
    try {
      var gateway =
          new OpenMeteoWeatherGateway(
              "http://127.0.0.1:" + server.getAddress().getPort() + "/forecast");
      var forecast = gateway.currentLima();
      assertEquals("Lima", forecast.location());
      assertEquals(21.5, forecast.temperatureC());
      assertEquals(0.2, forecast.precipitationMm());
      assertEquals(12, forecast.windKmh());
      assertEquals(forecast, gateway.currentLima());
      assertEquals(1, requests.get());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void translatesProviderFailureIntoStableApplicationError() throws Exception {
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/forecast",
        e -> {
          e.sendResponseHeaders(429, -1);
          e.close();
        });
    server.start();
    try {
      var error =
          assertThrows(
              DomainViolation.class,
              () ->
                  new OpenMeteoWeatherGateway(
                          "http://127.0.0.1:" + server.getAddress().getPort() + "/forecast")
                      .currentLima());
      assertEquals("UPSTREAM_UNAVAILABLE", error.code());
    } finally {
      server.stop(0);
    }
  }
}
