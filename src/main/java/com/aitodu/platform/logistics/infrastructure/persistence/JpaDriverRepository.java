package com.aitodu.platform.logistics.infrastructure.persistence;

import com.aitodu.platform.logistics.domain.model.aggregates.Driver;
import com.aitodu.platform.logistics.domain.repositories.DriverRepository;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaDriverRepository implements DriverRepository {
  @PersistenceContext private EntityManager em;

  private Driver domain(DriverPersistenceEntity e) {
    return new Driver(e.id, e.tenantId, e.name, e.license);
  }

  public Optional<Driver> find(UUID t, UUID id) {
    return Optional.ofNullable(em.find(DriverPersistenceEntity.class, id))
        .filter(e -> e.tenantId.equals(t))
        .map(this::domain);
  }

  public List<Driver> findAll(UUID t) {
    return em.createQuery(
            "from DriverPersistenceEntity where tenantId=:t order by name",
            DriverPersistenceEntity.class)
        .setParameter("t", t)
        .getResultStream()
        .map(this::domain)
        .toList();
  }

  public void save(Driver a) {
    var e = new DriverPersistenceEntity();
    e.id = a.id();
    e.tenantId = a.tenantId();
    e.name = a.name();
    e.license = a.license();
    em.persist(e);
  }
}
