package com.aitodu.platform.returns.infrastructure.persistence;

import com.aitodu.platform.returns.domain.model.aggregates.CustomerReturn;
import com.aitodu.platform.returns.domain.repositories.ReturnRepository;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaReturnRepository implements ReturnRepository {
  @PersistenceContext private EntityManager em;

  public void save(CustomerReturn a) {
    var e = new ReturnPersistenceEntity();
    e.id = a.id();
    e.tenantId = a.tenantId();
    e.saleId = a.saleId();
    e.saleLineId = a.saleLineId();
    e.productId = a.productId();
    e.quantity = a.quantity();
    e.disposition = a.disposition();
    e.reason = a.reason();
    e.createdAt = a.createdAt();
    e.lossCents = a.lossCents();
    em.persist(e);
  }

  public List<CustomerReturn> findAll(UUID t) {
    return em.createQuery(
            "from ReturnPersistenceEntity where tenantId=:t order by createdAt desc",
            ReturnPersistenceEntity.class)
        .setParameter("t", t)
        .getResultStream()
        .map(
            e ->
                new CustomerReturn(
                    e.id,
                    e.tenantId,
                    e.saleId,
                    e.saleLineId,
                    e.productId,
                    e.quantity,
                    e.disposition,
                    e.reason,
                    e.createdAt,
                    e.lossCents))
        .toList();
  }
}
