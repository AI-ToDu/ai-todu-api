package com.aitodu.platform.iam.infrastructure.security;

import com.aitodu.platform.iam.application.PasswordHasher;
import com.aitodu.platform.iam.domain.model.aggregates.UserAccount;
import com.aitodu.platform.iam.domain.model.valueobjects.EmailAddress;
import com.aitodu.platform.iam.domain.repositories.AccountRepository;
import com.aitodu.platform.shared.domain.DomainViolation;
import jakarta.persistence.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService
    implements com.aitodu.platform.iam.application.SessionCommandService,
        com.aitodu.platform.iam.application.SessionQueryService {
  @PersistenceContext private EntityManager em;
  private final AccountRepository accounts;
  private final PasswordHasher passwords;
  private final Duration lifetime;
  private final javax.crypto.SecretKey signingKey;

  public SessionService(
      AccountRepository a,
      PasswordHasher p,
      @Value("${app.session.hours}") long hours,
      @Value("${app.jwt.secret}") String secret) {
    accounts = a;
    passwords = p;
    lifetime = Duration.ofHours(hours);
    signingKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
  }

  @Transactional
  public String login(String email, String password) {
    var u =
        accounts
            .findByEmail(new EmailAddress(email))
            .orElseThrow(() -> new DomainViolation("INVALID_CREDENTIALS"));
    DomainViolation.require(
        u.active() && passwords.matches(password, u.passwordHash()), "INVALID_CREDENTIALS");
    Instant issuedAt = Instant.now();
    String token =
        io.jsonwebtoken.Jwts.builder()
            .issuer("ai-todu")
            .subject(u.id().toString())
            .id(UUID.randomUUID().toString())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(issuedAt.plus(lifetime)))
            .signWith(signingKey, io.jsonwebtoken.Jwts.SIG.HS256)
            .compact();
    var s = new SessionPersistenceEntity();
    s.tokenHash = digest(token);
    s.userId = u.id();
    s.expiresAt = Instant.now().plus(lifetime);
    em.persist(s);
    return token;
  }

  @Transactional(readOnly = true)
  public WorkspacePrincipal authenticate(String token) {
    try {
      io.jsonwebtoken.Jwts.parser()
          .verifyWith(signingKey)
          .requireIssuer("ai-todu")
          .build()
          .parseSignedClaims(token);
    } catch (io.jsonwebtoken.JwtException | IllegalArgumentException invalid) {
      throw new DomainViolation("UNAUTHENTICATED");
    }
    var s = em.find(SessionPersistenceEntity.class, digest(token));
    DomainViolation.require(s != null && s.expiresAt.isAfter(Instant.now()), "UNAUTHENTICATED");
    var u =
        accounts
            .findById(s.userId)
            .filter(UserAccount::active)
            .orElseThrow(() -> new DomainViolation("UNAUTHENTICATED"));
    return new WorkspacePrincipal(u.id(), u.tenantId(), u.name(), u.email().value(), u.role());
  }

  @Transactional
  public void logout(String token) {
    var s = em.find(SessionPersistenceEntity.class, digest(token));
    if (s != null) em.remove(s);
  }

  private static String digest(String s) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
