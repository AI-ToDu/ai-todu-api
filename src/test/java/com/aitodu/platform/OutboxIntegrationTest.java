package com.aitodu.platform;

import static org.junit.jupiter.api.Assertions.*;

import com.aitodu.platform.iam.domain.repositories.AccountRepository;
import com.aitodu.platform.shared.application.*;
import com.aitodu.platform.shared.domain.DomainViolation;
import com.aitodu.platform.shared.infrastructure.outbox.OutboxRelay;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(
    properties = {
      "spring.datasource.url=${TEST_DATABASE_URL:jdbc:h2:mem:outbox;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1}",
      "spring.datasource.username=${TEST_DATABASE_USER:sa}",
      "spring.datasource.password=${TEST_DATABASE_PASSWORD:}",
      "app.outbox.initial-delay-ms=86400000"
    })
@Import(OutboxIntegrationTest.FaultConfiguration.class)
class OutboxIntegrationTest {
  @Autowired CommandExecutor commands;
  @Autowired WorkspaceLock locks;
  @Autowired AccountRepository accounts;
  @Autowired IntegrationOutbox outbox;
  @Autowired OutboxRelay relay;
  @Autowired JdbcTemplate db;
  @Autowired FaultProbe probe;

  static class FaultProbe implements IntegrationSubscriber {
    final AtomicBoolean fail = new AtomicBoolean();

    public String name() {
      return "test.fault-probe";
    }

    public void accept(com.aitodu.platform.shared.interfaces.events.IntegrationMessage m) {
      if (fail.get()) throw new IllegalStateException("Injected delivery failure");
    }
  }

  @TestConfiguration
  static class FaultConfiguration {
    @Bean
    FaultProbe faultProbe() {
      return new FaultProbe();
    }
  }

  private UUID tenant() {
    var t = UUID.randomUUID();
    commands.create(
        () -> {
          accounts.createWorkspace(t, "Outbox test");
          locks.create(t);
          return t;
        });
    return t;
  }

  @Test
  void retryAfterFailureAndDuplicateDeliveryAreDurable() {
    var tenant = tenant();
    var id = UUID.randomUUID();
    commands.execute(
        tenant,
        () -> {
          outbox.append(
              id,
              tenant,
              "sales.sale-confirmed",
              1,
              Instant.now(),
              Map.of("saleId", UUID.randomUUID().toString(), "totalCents", 100, "currency", "PEN"));
          return true;
        });
    probe.fail.set(true);
    try {
      relay.relay();
    } finally {
      probe.fail.set(false);
    }
    assertEquals(
        "PENDING",
        db.queryForObject("select status from integration_outbox where id=?", String.class, id));
    assertEquals(
        1,
        db.queryForObject("select attempts from integration_outbox where id=?", Integer.class, id));
    assertEquals(
        0, db.queryForObject("select count(*) from notifications where id=?", Integer.class, id));
    db.update(
        "update integration_outbox set next_attempt_at=? where id=?",
        java.sql.Timestamp.from(Instant.now().minusSeconds(1)),
        id);
    relay.relay();
    assertEquals(
        "PUBLISHED",
        db.queryForObject("select status from integration_outbox where id=?", String.class, id));
    assertEquals(
        1, db.queryForObject("select count(*) from notifications where id=?", Integer.class, id));
    db.update(
        "update integration_outbox set status='PENDING',next_attempt_at=? where id=?",
        java.sql.Timestamp.from(Instant.now().minusSeconds(1)),
        id);
    relay.relay();
    assertEquals(
        1, db.queryForObject("select count(*) from notifications where id=?", Integer.class, id));
  }

  @Test
  void messageRollsBackWithFailedCommand() {
    var tenant = tenant();
    var id = UUID.randomUUID();
    var result =
        commands.execute(
            tenant,
            () -> {
              outbox.append(id, tenant, "sales.sale-confirmed", 1, Instant.now(), Map.of());
              throw new DomainViolation("INSUFFICIENT_STOCK");
            });
    assertInstanceOf(com.aitodu.platform.shared.application.result.Result.Failure.class, result);
    assertEquals(
        0,
        db.queryForObject("select count(*) from integration_outbox where id=?", Integer.class, id));
  }
}
