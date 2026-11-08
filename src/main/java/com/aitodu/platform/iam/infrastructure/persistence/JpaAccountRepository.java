package com.aitodu.platform.iam.infrastructure.persistence;

import com.aitodu.platform.iam.domain.model.aggregates.UserAccount;
import com.aitodu.platform.iam.domain.model.valueobjects.EmailAddress;
import com.aitodu.platform.iam.domain.repositories.AccountRepository;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaAccountRepository implements AccountRepository {
  @PersistenceContext private EntityManager em;

  public Optional<UserAccount> findByEmail(EmailAddress email) {
    return em.createQuery(
            "from UserPersistenceEntity where email=:email", UserPersistenceEntity.class)
        .setParameter("email", email.value())
        .getResultStream()
        .findFirst()
        .map(AccountPersistenceAssembler::toDomain);
  }

  public Optional<UserAccount> findById(UUID id) {
    return Optional.ofNullable(em.find(UserPersistenceEntity.class, id))
        .map(AccountPersistenceAssembler::toDomain);
  }

  public List<UserAccount> findAll(UUID tenant) {
    return em.createQuery(
            "from UserPersistenceEntity where tenantId=:tenant order by name",
            UserPersistenceEntity.class)
        .setParameter("tenant", tenant)
        .getResultStream()
        .map(AccountPersistenceAssembler::toDomain)
        .toList();
  }

  public void save(UserAccount user) {
    em.merge(AccountPersistenceAssembler.toPersistence(user));
  }

  public void createWorkspace(UUID tenant, String name) {
    var w = new WorkspacePersistenceEntity();
    w.id = tenant;
    w.name = name;
    em.persist(w);
  }
}
