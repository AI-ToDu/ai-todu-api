package com.aitodu.platform.iam.domain.repositories;

import com.aitodu.platform.iam.domain.model.aggregates.UserAccount;
import com.aitodu.platform.iam.domain.model.valueobjects.EmailAddress;
import java.util.*;

public interface AccountRepository {
  Optional<UserAccount> findByEmail(EmailAddress email);

  Optional<UserAccount> findById(UUID id);

  List<UserAccount> findAll(UUID tenant);

  void save(UserAccount user);

  void createWorkspace(UUID tenant, String name);
}
