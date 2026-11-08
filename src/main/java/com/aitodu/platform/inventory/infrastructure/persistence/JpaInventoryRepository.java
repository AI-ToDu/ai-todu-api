package com.aitodu.platform.inventory.infrastructure.persistence;

import com.aitodu.platform.inventory.domain.model.aggregates.InventoryItem;
import com.aitodu.platform.inventory.domain.model.entities.StockMovement;
import com.aitodu.platform.inventory.domain.model.valueobjects.Sku;
import com.aitodu.platform.inventory.domain.repositories.InventoryRepository;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaInventoryRepository implements InventoryRepository {
  @PersistenceContext private EntityManager em;

  public List<InventoryItem> findAll(UUID t) {
    return em.createQuery(
            "from InventoryPersistenceEntity where tenantId=:t order by name",
            InventoryPersistenceEntity.class)
        .setParameter("t", t)
        .getResultStream()
        .map(InventoryPersistenceAssembler::toDomain)
        .toList();
  }

  public Optional<InventoryItem> find(UUID t, UUID id) {
    return Optional.ofNullable(em.find(InventoryPersistenceEntity.class, id))
        .filter(e -> e.tenantId.equals(t))
        .map(InventoryPersistenceAssembler::toDomain);
  }

  public boolean exists(UUID t, Sku sku) {
    return !em.createQuery(
            "from InventoryPersistenceEntity where tenantId=:t and sku=:sku",
            InventoryPersistenceEntity.class)
        .setParameter("t", t)
        .setParameter("sku", sku)
        .setMaxResults(1)
        .getResultList()
        .isEmpty();
  }

  public void save(InventoryItem a) {
    var e = em.find(InventoryPersistenceEntity.class, a.id());
    if (e == null) {
      e = new InventoryPersistenceEntity();
      InventoryPersistenceAssembler.copy(a, e);
      em.persist(e);
    } else InventoryPersistenceAssembler.copy(a, e);
  }

  public void record(StockMovement m) {
    var e = new MovementPersistenceEntity();
    e.id = m.id();
    e.tenantId = m.tenantId();
    e.productId = m.productId();
    e.actorId = m.actorId();
    e.delta = m.delta();
    e.type = m.type();
    e.reason = m.reason();
    e.createdAt = m.createdAt();
    em.persist(e);
  }

  public List<StockMovement> movements(UUID t) {
    return em.createQuery(
            "from MovementPersistenceEntity where tenantId=:t order by createdAt desc",
            MovementPersistenceEntity.class)
        .setParameter("t", t)
        .getResultStream()
        .map(
            e ->
                new StockMovement(
                    e.id,
                    e.tenantId,
                    e.productId,
                    e.delta,
                    e.type,
                    e.reason,
                    e.actorId,
                    e.createdAt))
        .toList();
  }
}
