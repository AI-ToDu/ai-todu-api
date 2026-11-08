package com.aitodu.platform.logistics.interfaces.rest;

import com.aitodu.platform.logistics.domain.services.WeatherForecastGateway;
import java.time.Instant;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/logistics/weather")
@PreAuthorize("hasAnyRole('ADMIN','LOGISTICS')")
public class WeatherController {
  private final WeatherForecastGateway gateway;

  public WeatherController(WeatherForecastGateway g) {
    gateway = g;
  }

  public record WeatherResource(
      String location,
      double temperatureC,
      double precipitationMm,
      double windKmh,
      Instant observedAt,
      Instant fetchedAt,
      String source,
      String attributionUrl) {}

  @GetMapping
  public WeatherResource current() {
    var f = gateway.currentLima();
    return new WeatherResource(
        f.location(),
        f.temperatureC(),
        f.precipitationMm(),
        f.windKmh(),
        f.observedAt(),
        f.fetchedAt(),
        "Open-Meteo",
        "https://open-meteo.com/");
  }
}
