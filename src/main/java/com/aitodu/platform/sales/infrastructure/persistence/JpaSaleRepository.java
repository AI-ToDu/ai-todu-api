package com.aitodu.platform.sales.infrastructure.persistence;

import com.aitodu.platform.sales.domain.model.aggregates.Sale;
import com.aitodu.platform.sales.domain.repositories.SaleRepository;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaSaleRepository implements SaleRepository {
  @PersistenceContext private EntityManager em;

  public Optional<Sale> find(UUID t, UUID id) {
    return Optional.ofNullable(em.find(SalePersistenceEntity.class, id))
        .filter(e -> e.tenantId.equals(t))
        .map(SalePersistenceAssembler::toDomain);
  }

  public List<Sale> findAll(UUID t) {
    return em.createQuery(
            "from SalePersistenceEntity where tenantId=:t order by createdAt desc",
            SalePersistenceEntity.class)
        .setParameter("t", t)
        .getResultStream()
        .map(SalePersistenceAssembler::toDomain)
        .toList();
  }

  public void save(Sale a) {
    var e = em.find(SalePersistenceEntity.class, a.id());
    if (e == null) {
      e = new SalePersistenceEntity();
      SalePersistenceAssembler.copy(a, e);
      em.persist(e);
    } else SalePersistenceAssembler.copy(a, e);
  }

  public Optional<Sale> findByRequestKey(UUID t, String key) {
    if (key == null) return Optional.empty();
    return em.createQuery(
            "from SalePersistenceEntity where tenantId=:t and requestKey=:k",
            SalePersistenceEntity.class)
        .setParameter("t", t)
        .setParameter("k", key)
        .getResultStream()
        .findFirst()
        .map(SalePersistenceAssembler::toDomain);
  }

  public void remember(UUID id, String key, String fingerprint) {
    var e = em.find(SalePersistenceEntity.class, id);
    e.requestKey = key;
    e.fingerprint = fingerprint;
  }

  public String fingerprint(UUID id) {
    return em.find(SalePersistenceEntity.class, id).fingerprint;
  }
}
