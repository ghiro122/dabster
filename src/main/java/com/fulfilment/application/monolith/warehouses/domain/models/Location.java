package com.fulfilment.application.monolith.warehouses.domain.models;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import java.util.List;

public class Location {
  public String identification;

  // maximum number of warehouses that can be created in this location
  public int maxNumberOfWarehouses;

  // maximum capacity of the location summing all the warehouse capacities
  public int maxCapacity;

  public Location(String identification, int maxNumberOfWarehouses, int maxCapacity) {
    this.identification = identification;
    this.maxNumberOfWarehouses = maxNumberOfWarehouses;
    this.maxCapacity = maxCapacity;
  }

  public void validateFits(List<Warehouse> activeWarehouses, Warehouse incoming) {
    if (activeWarehouses.size() >= maxNumberOfWarehouses) {
      throw new WarehouseValidationException(
          "Location " + identification + " already holds its maximum number of warehouses.");
    }

    int usedCapacity = activeWarehouses.stream().mapToInt(warehouse -> warehouse.capacity).sum();
    if (usedCapacity + incoming.capacity > maxCapacity) {
      throw new WarehouseValidationException(
          "Location " + identification + " does not have enough capacity left.");
    }
  }
}
