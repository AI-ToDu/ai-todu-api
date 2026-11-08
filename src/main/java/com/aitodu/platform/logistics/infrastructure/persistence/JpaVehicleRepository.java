package com.aitodu.platform.logistics.infrastructure.persistence;

import com.aitodu.platform.logistics.domain.model.aggregates.Vehicle;
import com.aitodu.platform.logistics.domain.repositories.VehicleRepository;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaVehicleRepository implements VehicleRepository {
  @PersistenceContext private EntityManager em;

  private Vehicle domain(VehiclePersistenceEntity e) {
    return new Vehicle(e.id, e.tenantId, e.name, e.plate);
  }

  public Optional<Vehicle> find(UUID t, UUID id) {
    return Optional.ofNullable(em.find(VehiclePersistenceEntity.class, id))
        .filter(e -> e.tenantId.equals(t))
        .map(this::domain);
  }

  public List<Vehicle> findAll(UUID t) {
    return em.createQuery(
            "from VehiclePersistenceEntity where tenantId=:t order by name",
            VehiclePersistenceEntity.class)
        .setParameter("t", t)
        .getResultStream()
        .map(this::domain)
        .toList();
  }

  public void save(Vehicle a) {
    var e = new VehiclePersistenceEntity();
    e.id = a.id();
    e.tenantId = a.tenantId();
    e.name = a.name();
    e.plate = a.plate();
    em.persist(e);
  }
}
