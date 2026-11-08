package com.aitodu.platform.iam.interfaces.rest;

import static com.aitodu.platform.iam.interfaces.rest.AccountResources.*;

import com.aitodu.platform.iam.application.AccountCommandService;
import com.aitodu.platform.iam.domain.model.commands.RegisterWorkspaceCommand;
import com.aitodu.platform.iam.infrastructure.security.*;
import com.aitodu.platform.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.aitodu.platform.shared.interfaces.rest.ResponseEntityAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AccountsController {
  private final AccountCommandService accounts;
  private final com.aitodu.platform.iam.application.SessionCommandService sessions;
  private final com.aitodu.platform.iam.application.SessionQueryService sessionQueries;

  public AccountsController(
      AccountCommandService a,
      com.aitodu.platform.iam.application.SessionCommandService s,
      com.aitodu.platform.iam.application.SessionQueryService q) {
    accounts = a;
    sessions = s;
    sessionQueries = q;
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@jakarta.validation.Valid @RequestBody RegistrationResource r) {
    return ResponseEntityAssembler.from(
        accounts.handle(
            new RegisterWorkspaceCommand(r.name(), r.company(), r.email(), r.password())),
        UserResourceFromEntityAssembler::toResourceFromEntity,
        201);
  }

  @PostMapping("/login")
  public SessionResource login(@jakarta.validation.Valid @RequestBody LoginResource r) {
    String token = sessions.login(r.email(), r.password());
    return new SessionResource(
        token,
        UserResourceFromEntityAssembler.toResourceFromEntity(sessionQueries.authenticate(token)));
  }

  @GetMapping("/me")
  public UserResource me(@AuthenticationPrincipal WorkspacePrincipal p) {
    return UserResourceFromEntityAssembler.toResourceFromEntity(p);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader("Authorization") String auth) {
    sessions.logout(auth.substring(7));
    return ResponseEntity.noContent().build();
  }
}
