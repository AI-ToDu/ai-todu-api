package com.aitodu.platform.shared.interfaces.rest;

import com.aitodu.platform.shared.domain.DomainViolation;
import java.util.*;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  public static int status(String code) {
    if (code.equals("UNAUTHENTICATED") || code.equals("INVALID_CREDENTIALS")) return 401;
    if (code.equals("FORBIDDEN")) return 403;
    if (code.equals("NOT_FOUND")) return 404;
    if (code.endsWith("_EXISTS")
        || Set.of(
                "INSUFFICIENT_STOCK",
                "RESOURCE_BUSY",
                "SALE_DISPATCHED",
                "EXCESS_RETURN",
                "DUPLICATE_LINE",
                "ALREADY_CANCELLED",
                "EXCESS_RECEIPT",
                "IDEMPOTENCY_CONFLICT")
            .contains(code)) return 409;
    if (code.equals("UPSTREAM_UNAVAILABLE")) return 503;
    if (code.equals("INTERNAL_ERROR")) return 500;
    return 422;
  }

  public static ProblemDetail problem(String code) {
    return problem(code, status(code));
  }

  private static ProblemDetail problem(String code, int status) {
    var p = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status), message(code));
    p.setType(java.net.URI.create("about:blank"));
    p.setTitle(HttpStatus.valueOf(status).getReasonPhrase());
    p.setProperty("code", code);
    return p;
  }

  private static String message(String code) {
    try {
      return ResourceBundle.getBundle("messages", LocaleContextHolder.getLocale()).getString(code);
    } catch (MissingResourceException e) {
      return LocaleContextHolder.getLocale().getLanguage().equals("es")
          ? "La operación no se pudo completar. Revise los datos."
          : "The operation could not be completed. Check the supplied data.";
    }
  }

  @ExceptionHandler(DomainViolation.class)
  ResponseEntity<ProblemDetail> domain(DomainViolation error) {
    var p = problem(error.code());
    return ResponseEntity.status(p.getStatus()).body(p);
  }

  @ExceptionHandler({
    org.springframework.web.bind.MethodArgumentNotValidException.class,
    org.springframework.web.method.annotation.HandlerMethodValidationException.class,
    IllegalArgumentException.class,
    org.springframework.http.converter.HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<ProblemDetail> invalid(Exception e) {
    var p = problem("INVALID_TEXT", 400);
    return ResponseEntity.badRequest().body(p);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ProblemDetail> conflict(Exception e) {
    var p = problem("CONFLICT", 409);
    return ResponseEntity.status(409).body(p);
  }

  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  ResponseEntity<ProblemDetail> forbidden(Exception e) {
    return ResponseEntity.status(403).body(problem("FORBIDDEN"));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ProblemDetail> unexpected(Exception e) {
    org.slf4j.LoggerFactory.getLogger(getClass()).error("Request failed", e);
    var p = problem("INTERNAL_ERROR");
    return ResponseEntity.internalServerError().body(p);
  }
}
