package com.aitodu.platform.shared.application.result;

import com.aitodu.platform.shared.domain.DomainViolation;
import java.util.function.Supplier;

public sealed interface Result<T> permits Result.Success, Result.Failure {
  record Success<T>(T value) implements Result<T> {}

  record Failure<T>(ApplicationError error) implements Result<T> {}

  static <T> Result<T> attempt(Supplier<T> action) {
    try {
      return new Success<>(action.get());
    } catch (DomainViolation e) {
      return new Failure<>(new ApplicationError(e.code()));
    }
  }
}
