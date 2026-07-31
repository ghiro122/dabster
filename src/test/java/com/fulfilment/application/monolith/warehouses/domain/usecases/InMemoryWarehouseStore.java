package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;

/** Stores and returns copies, so that callers can only change the state through the port. */
public class InMemoryWarehouseStore implements WarehouseStore {

  private final List<Warehouse> warehouses = new ArrayList<>();

  private long nextId = 1;

  private int createCount;

  private int updateCount;

  public int createCount() {
    return createCount;
  }

  public int updateCount() {
    return updateCount;
  }

  @Override
  public List<Warehouse> getAll() {
    return warehouses.stream()
        .filter(warehouse -> warehouse.archivedAt == null)
        .map(InMemoryWarehouseStore::copyOf)
        .toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    createCount++;

    seed(warehouse);
  }

  /** Sets up existing data without counting as a call made by the code under test. */
  public void seed(Warehouse warehouse) {
    warehouse.id = nextId++;
    warehouses.add(copyOf(warehouse));
  }

  @Override
  public void update(Warehouse warehouse) {
    updateCount++;

    for (int i = 0; i < warehouses.size(); i++) {
      if (warehouses.get(i).id.equals(warehouse.id)) {
        warehouses.set(i, copyOf(warehouse));
        return;
      }
    }

    throw new IllegalStateException("No warehouse with id of " + warehouse.id + " to update.");
  }

  @Override
  public void remove(Warehouse warehouse) {
    warehouses.removeIf(stored -> stored.id.equals(warehouse.id));
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    return warehouses.stream()
        .filter(warehouse -> warehouse.archivedAt == null)
        .filter(warehouse -> warehouse.businessUnitCode.equals(buCode))
        .findFirst()
        .map(InMemoryWarehouseStore::copyOf)
        .orElse(null);
  }

  @Override
  public boolean existsByBusinessUnitCode(String buCode) {
    return warehouses.stream().anyMatch(warehouse -> warehouse.businessUnitCode.equals(buCode));
  }

  @Override
  public Warehouse findByTechnicalId(Long id) {
    return warehouses.stream()
        .filter(warehouse -> warehouse.id.equals(id))
        .findFirst()
        .map(InMemoryWarehouseStore::copyOf)
        .orElse(null);
  }

  @Override
  public List<Warehouse> findActiveByLocation(String locationIdentifier) {
    return warehouses.stream()
        .filter(warehouse -> warehouse.archivedAt == null)
        .filter(warehouse -> warehouse.location.equals(locationIdentifier))
        .map(InMemoryWarehouseStore::copyOf)
        .toList();
  }

  private static Warehouse copyOf(Warehouse warehouse) {
    var copy = new Warehouse();
    copy.id = warehouse.id;
    copy.businessUnitCode = warehouse.businessUnitCode;
    copy.location = warehouse.location;
    copy.capacity = warehouse.capacity;
    copy.stock = warehouse.stock;
    copy.createdAt = warehouse.createdAt;
    copy.archivedAt = warehouse.archivedAt;
    return copy;
  }
}
