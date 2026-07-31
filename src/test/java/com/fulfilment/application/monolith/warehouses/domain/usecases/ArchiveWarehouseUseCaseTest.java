package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

  private final InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();

  private final ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(warehouseStore);

  @Test
  public void testArchiveActiveWarehouse() {
    var stored = activeWarehouse();
    warehouseStore.create(stored);

    useCase.archive(warehouseWithId(stored.id));

    var archived = warehouseStore.findByTechnicalId(stored.id);
    assertNotNull(archived);
    assertNotNull(archived.archivedAt);
    assertEquals("MWH.001", archived.businessUnitCode);
    assertEquals(1, warehouseStore.updateCount());
    assertEquals(0, warehouseStore.getAll().size());
  }

  @Test
  public void testArchiveAlreadyArchivedWarehouseShouldNotFailNorUpdate() {
    var archivedAt = LocalDateTime.of(2024, 7, 1, 10, 0);
    var stored = activeWarehouse();
    stored.archivedAt = archivedAt;
    warehouseStore.create(stored);

    useCase.archive(warehouseWithId(stored.id));

    assertEquals(0, warehouseStore.updateCount());
    assertEquals(archivedAt, warehouseStore.findByTechnicalId(stored.id).archivedAt);
  }

  @Test
  public void testArchiveUnknownTechnicalIdShouldFail() {
    assertThrows(WarehouseNotFoundException.class, () -> useCase.archive(warehouseWithId(404L)));

    assertEquals(0, warehouseStore.updateCount());
  }

  @Test
  public void testArchiveWithoutTechnicalIdShouldFail() {
    assertThrows(WarehouseValidationException.class, () -> useCase.archive(new Warehouse()));
  }

  private static Warehouse activeWarehouse() {
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 50;
    warehouse.stock = 10;
    warehouse.createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
    return warehouse;
  }

  private static Warehouse warehouseWithId(Long id) {
    var warehouse = new Warehouse();
    warehouse.id = id;
    return warehouse;
  }
}
