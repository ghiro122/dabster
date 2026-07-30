package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  @Transactional
  public void archive(Warehouse warehouse) {
    if (warehouse == null) {
      throw new WarehouseValidationException("Warehouse is required.");
    }
    if (warehouse.id == null) {
      throw new WarehouseValidationException("Warehouse id is required to archive a warehouse.");
    }

    Warehouse storedWarehouse = warehouseStore.findByTechnicalId(warehouse.id);
    if (storedWarehouse == null) {
      throw new WarehouseNotFoundException(
          "Warehouse with id of " + warehouse.id + " does not exist.");
    }

    if (storedWarehouse.archivedAt != null) {
      return;
    }

    storedWarehouse.archivedAt = LocalDateTime.now();

    warehouseStore.update(storedWarehouse);
  }
}
