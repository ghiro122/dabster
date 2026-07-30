package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    if (warehouse == null) {
      throw new WarehouseValidationException("Warehouse is required.");
    }
    if (warehouse.id != null) {
      throw new WarehouseValidationException("Warehouse id must not be set on creation.");
    }
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new WarehouseValidationException("Warehouse business unit code is required.");
    }
    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw new WarehouseValidationException("Warehouse location is required.");
    }

    warehouse.validateCapacityAndStock();

    Location location = resolveLocation(warehouse.location);

    if (warehouseStore.existsByBusinessUnitCode(warehouse.businessUnitCode)) {
      throw new WarehouseValidationException(
          "Warehouse business unit code "
              + warehouse.businessUnitCode
              + " is already used by another warehouse.");
    }

    location.validateFits(warehouseStore.findActiveByLocation(warehouse.location), warehouse);

    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;

    warehouseStore.create(warehouse);
  }

  private Location resolveLocation(String identifier) {
    try {
      return locationResolver.resolveByIdentifier(identifier);
    } catch (LocationNotFoundException e) {
      throw new WarehouseValidationException(e.getMessage(), e);
    }
  }
}
