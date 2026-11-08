package com.aitodu.platform.logistics.infrastructure.external;

import com.aitodu.platform.logistics.domain.services.WeatherForecastGateway;
import com.aitodu.platform.shared.domain.DomainViolation;
import java.net.*;
import java.net.http.*;
import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class OpenMeteoWeatherGateway implements WeatherForecastGateway {
  private final String endpoint;
  private final HttpClient client =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
  private final JsonMapper json = JsonMapper.builder().build();
  private volatile Forecast cached;

  public OpenMeteoWeatherGateway(@Value("${app.weather.url}") String endpoint) {
    this.endpoint = endpoint;
  }

  public synchronized Forecast currentLima() {
    if (cached != null && cached.fetchedAt().plusSeconds(600).isAfter(Instant.now())) return cached;
    try {
      var uri =
          URI.create(
              endpoint
                  + "?latitude=-12.0464&longitude=-77.0428&current=temperature_2m,precipitation,wind_speed_10m&timezone=UTC&forecast_days=1");
      var response =
          client.send(
              HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5)).GET().build(),
              HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) throw new DomainViolation("UPSTREAM_UNAVAILABLE");
      var current = json.readTree(response.body()).path("current");
      for (var field : java.util.List.of("temperature_2m", "precipitation", "wind_speed_10m"))
        if (!current.path(field).isNumber()) throw new DomainViolation("UPSTREAM_UNAVAILABLE");
      cached =
          new Forecast(
              "Lima",
              current.path("temperature_2m").asDouble(),
              current.path("precipitation").asDouble(),
              current.path("wind_speed_10m").asDouble(),
              LocalDateTime.parse(current.path("time").asText()).toInstant(ZoneOffset.UTC),
              Instant.now());
      return cached;
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new DomainViolation("UPSTREAM_UNAVAILABLE");
    } catch (Exception unavailable) {
      throw new DomainViolation("UPSTREAM_UNAVAILABLE");
    }
  }
}
