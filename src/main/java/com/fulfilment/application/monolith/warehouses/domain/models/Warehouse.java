package com.fulfilment.application.monolith.warehouses.domain.models;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import java.time.LocalDateTime;

public class Warehouse {

  public Long id;

  // unique identifier
  public String businessUnitCode;

  public String location;

  public Integer capacity;

  public Integer stock;

  public LocalDateTime createdAt;

  public LocalDateTime archivedAt;

  public void validateCapacityAndStock() {
    if (capacity == null || capacity <= 0) {
      throw new WarehouseValidationException("Warehouse capacity must be greater than zero.");
    }
    if (stock == null || stock < 0) {
      throw new WarehouseValidationException("Warehouse stock must not be negative.");
    }
    if (stock > capacity) {
      throw new WarehouseValidationException("Warehouse stock must not exceed its capacity.");
    }
  }
}
