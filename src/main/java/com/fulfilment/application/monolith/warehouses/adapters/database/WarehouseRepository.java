package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.list("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    var dbWarehouse = new DbWarehouse();
    copyModifiableFields(warehouse, dbWarehouse);
    dbWarehouse.createdAt = warehouse.createdAt;

    this.persist(dbWarehouse);

    warehouse.id = dbWarehouse.id;
  }

  @Override
  public void update(Warehouse warehouse) {
    if (warehouse.id == null) {
      throw new IllegalArgumentException("Warehouse to update has no technical id.");
    }

    DbWarehouse dbWarehouse = this.findById(warehouse.id);
    if (dbWarehouse == null) {
      throw new IllegalStateException(
          "Warehouse with id of " + warehouse.id + " does not exist anymore.");
    }

    copyModifiableFields(warehouse, dbWarehouse);
  }

  @Override
  public void remove(Warehouse warehouse) {
    if (warehouse.id == null) {
      throw new IllegalArgumentException("Warehouse to remove has no technical id.");
    }

    this.deleteById(warehouse.id);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse =
        this.find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
    return dbWarehouse == null ? null : dbWarehouse.toWarehouse();
  }

  @Override
  public boolean existsByBusinessUnitCode(String buCode) {
    return this.count("businessUnitCode", buCode) > 0;
  }

  @Override
  public Warehouse findByTechnicalId(Long id) {
    DbWarehouse dbWarehouse = this.findById(id);
    return dbWarehouse == null ? null : dbWarehouse.toWarehouse();
  }

  @Override
  public List<Warehouse> findActiveByLocation(String locationIdentifier) {
    return this.list("location = ?1 and archivedAt is null", locationIdentifier).stream()
        .map(DbWarehouse::toWarehouse)
        .toList();
  }

  private void copyModifiableFields(Warehouse warehouse, DbWarehouse dbWarehouse) {
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.archivedAt = warehouse.archivedAt;
  }
}
