package com.aitodu.platform.shared.interfaces.rest;

import com.aitodu.platform.shared.application.result.Result;
import com.aitodu.platform.shared.domain.DomainViolation;
import java.util.function.Function;
import org.springframework.http.ResponseEntity;

public final class ResponseEntityAssembler {
  private ResponseEntityAssembler() {}

  public static <T, R> ResponseEntity<R> from(
      Result<T> result, Function<T, R> resource, int status) {
    if (result instanceof Result.Success<T> success)
      return ResponseEntity.status(status).body(resource.apply(success.value()));
    throw new DomainViolation(((Result.Failure<T>) result).error().code());
  }
}
