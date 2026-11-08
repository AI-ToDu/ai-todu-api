package com.aitodu.platform.logistics.domain.services;

import java.time.Instant;

public interface WeatherForecastGateway {
  Forecast currentLima();

  record Forecast(
      String location,
      double temperatureC,
      double precipitationMm,
      double windKmh,
      Instant observedAt,
      Instant fetchedAt) {}
}
