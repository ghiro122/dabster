package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class DbWarehouseTest {

  @Test
  public void testToWarehouseShouldCopyEveryFieldIncludingTheTechnicalId() {
    var createdAt = LocalDateTime.of(2024, 7, 1, 10, 0);
    var archivedAt = LocalDateTime.of(2025, 7, 1, 10, 0);

    var dbWarehouse = new DbWarehouse();
    dbWarehouse.id = 42L;
    dbWarehouse.businessUnitCode = "MWH.001";
    dbWarehouse.location = "ZWOLLE-001";
    dbWarehouse.capacity = 100;
    dbWarehouse.stock = 10;
    dbWarehouse.createdAt = createdAt;
    dbWarehouse.archivedAt = archivedAt;

    Warehouse warehouse = dbWarehouse.toWarehouse();

    assertEquals(42L, warehouse.id);
    assertEquals("MWH.001", warehouse.businessUnitCode);
    assertEquals("ZWOLLE-001", warehouse.location);
    assertEquals(100, warehouse.capacity);
    assertEquals(10, warehouse.stock);
    assertEquals(createdAt, warehouse.createdAt);
    assertEquals(archivedAt, warehouse.archivedAt);
  }
}
