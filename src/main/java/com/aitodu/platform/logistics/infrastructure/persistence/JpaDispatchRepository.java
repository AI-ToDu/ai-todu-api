package com.aitodu.platform.logistics.infrastructure.persistence;

import com.aitodu.platform.logistics.domain.model.aggregates.DispatchManifest;
import com.aitodu.platform.logistics.domain.repositories.DispatchRepository;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaDispatchRepository implements DispatchRepository {
  @PersistenceContext private EntityManager em;

  public Optional<DispatchManifest> find(UUID t, UUID id) {
    return Optional.ofNullable(em.find(DispatchPersistenceEntity.class, id))
        .filter(e -> e.tenantId.equals(t))
        .map(DispatchPersistenceAssembler::toDomain);
  }

  public List<DispatchManifest> findAll(UUID t) {
    return em.createQuery(
            "from DispatchPersistenceEntity where tenantId=:t order by createdAt desc",
            DispatchPersistenceEntity.class)
        .setParameter("t", t)
        .getResultStream()
        .map(DispatchPersistenceAssembler::toDomain)
        .toList();
  }

  public void save(DispatchManifest a) {
    var e = em.find(DispatchPersistenceEntity.class, a.id());
    if (e == null) {
      e = new DispatchPersistenceEntity();
      DispatchPersistenceAssembler.copy(a, e);
      em.persist(e);
    } else DispatchPersistenceAssembler.copy(a, e);
  }

  public boolean busy(UUID t, UUID driver, UUID vehicle) {
    return em.createQuery(
                "select count(d) from DispatchPersistenceEntity d where d.tenantId=:t and"
                    + " d.status='IN_TRANSIT' and (d.driverId=:driver or d.vehicleId=:vehicle)",
                Long.class)
            .setParameter("t", t)
            .setParameter("driver", driver)
            .setParameter("vehicle", vehicle)
            .getSingleResult()
        > 0;
  }
}
