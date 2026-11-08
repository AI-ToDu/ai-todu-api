package com.aitodu.platform.procurement.infrastructure.persistence;

import com.aitodu.platform.procurement.domain.model.aggregates.Supplier;
import com.aitodu.platform.procurement.domain.repositories.SupplierRepository;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaSupplierRepository implements SupplierRepository {
  @PersistenceContext private EntityManager em;

  private Supplier domain(SupplierPersistenceEntity e) {
    return new Supplier(e.id, e.tenantId, e.name, e.contact);
  }

  public Optional<Supplier> find(UUID t, UUID id) {
    return Optional.ofNullable(em.find(SupplierPersistenceEntity.class, id))
        .filter(e -> e.tenantId.equals(t))
        .map(this::domain);
  }

  public List<Supplier> findAll(UUID t) {
    return em.createQuery(
            "from SupplierPersistenceEntity where tenantId=:t order by name",
            SupplierPersistenceEntity.class)
        .setParameter("t", t)
        .getResultStream()
        .map(this::domain)
        .toList();
  }

  public void save(Supplier a) {
    var e = new SupplierPersistenceEntity();
    e.id = a.id();
    e.tenantId = a.tenantId();
    e.name = a.name();
    e.contact = a.contact();
    em.persist(e);
  }
}
