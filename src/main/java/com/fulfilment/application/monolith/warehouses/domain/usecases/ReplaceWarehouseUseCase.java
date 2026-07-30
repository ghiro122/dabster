package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  @Transactional
  public void replace(Warehouse newWarehouse) {
    if (newWarehouse == null) {
      throw new WarehouseValidationException("Warehouse is required.");
    }
    if (newWarehouse.id != null) {
      throw new WarehouseValidationException("Warehouse id must not be set on a replacement.");
    }
    if (newWarehouse.businessUnitCode == null || newWarehouse.businessUnitCode.isBlank()) {
      throw new WarehouseValidationException("Warehouse business unit code is required.");
    }
    if (newWarehouse.location == null || newWarehouse.location.isBlank()) {
      throw new WarehouseValidationException("Warehouse location is required.");
    }

    Warehouse replacedWarehouse =
        warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (replacedWarehouse == null) {
      throw new WarehouseNotFoundException(
          "There is no active warehouse with business unit code "
              + newWarehouse.businessUnitCode
              + " to replace.");
    }

    newWarehouse.validateCapacityAndStock();

    if (!newWarehouse.stock.equals(replacedWarehouse.stock)) {
      throw new WarehouseValidationException(
          "The replacing warehouse must hold the same stock as the warehouse being replaced.");
    }

    Location location = resolveLocation(newWarehouse.location);
    location.validateFits(
        activeAtTargetLocationExcluding(newWarehouse.location, replacedWarehouse), newWarehouse);

    var replacedAt = LocalDateTime.now();

    replacedWarehouse.archivedAt = replacedAt;
    warehouseStore.update(replacedWarehouse);

    newWarehouse.id = null;
    newWarehouse.businessUnitCode = replacedWarehouse.businessUnitCode;
    newWarehouse.createdAt = replacedAt;
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }

  private List<Warehouse> activeAtTargetLocationExcluding(
      String locationIdentifier, Warehouse excludedWarehouse) {
    return warehouseStore.findActiveByLocation(locationIdentifier).stream()
        .filter(warehouse -> !warehouse.id.equals(excludedWarehouse.id))
        .toList();
  }

  private Location resolveLocation(String identifier) {
    try {
      return locationResolver.resolveByIdentifier(identifier);
    } catch (LocationNotFoundException e) {
      throw new WarehouseValidationException(e.getMessage(), e);
    }
  }
}
