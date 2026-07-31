package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.RetrieveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class RetrieveWarehouseUseCase implements RetrieveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public RetrieveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public List<Warehouse> getAll() {
    return warehouseStore.getAll();
  }

  @Override
  public Warehouse getById(Long id) {
    if (id == null) {
      throw new WarehouseValidationException("Warehouse id is required.");
    }

    Warehouse warehouse = warehouseStore.findByTechnicalId(id);
    if (warehouse == null || warehouse.archivedAt != null) {
      throw new WarehouseNotFoundException("Warehouse with id of " + id + " does not exist.");
    }

    return warehouse;
  }
}
