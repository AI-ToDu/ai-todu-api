package com.aitodu.platform.procurement.infrastructure.persistence;

import com.aitodu.platform.procurement.domain.model.aggregates.PurchaseOrder;
import com.aitodu.platform.procurement.domain.model.entities.PurchaseLine;
import com.aitodu.platform.procurement.domain.repositories.PurchaseRepository;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaPurchaseRepository implements PurchaseRepository {
  @PersistenceContext private EntityManager em;

  private PurchaseOrder domain(PurchasePersistenceEntity e) {
    return new PurchaseOrder(
        e.id,
        e.tenantId,
        e.supplierId,
        e.lines.stream()
            .map(l -> new PurchaseLine(l.id, l.productId, l.quantity, l.received))
            .toList(),
        e.createdAt);
  }

  public Optional<PurchaseOrder> find(UUID t, UUID id) {
    return Optional.ofNullable(em.find(PurchasePersistenceEntity.class, id))
        .filter(e -> e.tenantId.equals(t))
        .map(this::domain);
  }

  public List<PurchaseOrder> findAll(UUID t) {
    return em.createQuery(
            "from PurchasePersistenceEntity where tenantId=:t order by createdAt desc",
            PurchasePersistenceEntity.class)
        .setParameter("t", t)
        .getResultStream()
        .map(this::domain)
        .toList();
  }

  public void save(PurchaseOrder a) {
    var e = em.find(PurchasePersistenceEntity.class, a.id());
    boolean fresh = e == null;
    if (fresh) e = new PurchasePersistenceEntity();
    e.id = a.id();
    e.tenantId = a.tenantId();
    e.supplierId = a.supplierId();
    e.createdAt = a.createdAt();
    e.lines.clear();
    for (var l : a.lines()) {
      var r = new PurchaseLineEmbeddable();
      r.id = l.id();
      r.productId = l.productId();
      r.quantity = l.quantity();
      r.received = l.received();
      e.lines.add(r);
    }
    if (fresh) em.persist(e);
  }
}
