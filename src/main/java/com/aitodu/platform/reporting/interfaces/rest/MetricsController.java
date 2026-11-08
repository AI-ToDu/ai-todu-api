package com.aitodu.platform.reporting.interfaces.rest;

import com.aitodu.platform.iam.infrastructure.security.WorkspacePrincipal;
import com.aitodu.platform.reporting.application.MetricsQueryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/metrics")
@PreAuthorize("hasRole('ADMIN')")
public class MetricsController {
  private final MetricsQueryService service;

  public MetricsController(MetricsQueryService s) {
    service = s;
  }

  @GetMapping
  public MetricsQueryService.Metrics metrics(@AuthenticationPrincipal WorkspacePrincipal p) {
    return service.handle(
        new com.aitodu.platform.reporting.domain.model.queries.GetMetricsQuery(p.tenantId()));
  }
}
