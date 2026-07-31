package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class RetrieveWarehouseUseCaseTest {

  private final InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();

  private final RetrieveWarehouseUseCase useCase = new RetrieveWarehouseUseCase(warehouseStore);

  @Test
  public void testGetAllShouldReturnOnlyActiveWarehouses() {
    warehouseStore.seed(warehouse("MWH.001", "AMSTERDAM-001", 50, 10));
    warehouseStore.seed(archived(warehouse("MWH.012", "AMSTERDAM-001", 30, 5)));

    var warehouses = useCase.getAll();

    assertEquals(1, warehouses.size());
    assertEquals("MWH.001", warehouses.get(0).businessUnitCode);
  }

  @Test
  public void testGetByIdShouldReturnTheActiveWarehouse() {
    var stored = warehouse("MWH.001", "AMSTERDAM-001", 50, 10);
    warehouseStore.seed(stored);

    var warehouse = useCase.getById(stored.id);

    assertEquals(stored.id, warehouse.id);
    assertEquals("MWH.001", warehouse.businessUnitCode);
    assertEquals("AMSTERDAM-001", warehouse.location);
  }

  @Test
  public void testGetByUnknownIdShouldFail() {
    assertThrows(WarehouseNotFoundException.class, () -> useCase.getById(404L));
  }

  @Test
  public void testGetByIdOfAnArchivedWarehouseShouldFail() {
    var stored = archived(warehouse("MWH.001", "AMSTERDAM-001", 50, 10));
    warehouseStore.seed(stored);

    assertThrows(WarehouseNotFoundException.class, () -> useCase.getById(stored.id));
  }

  @Test
  public void testGetByNullIdShouldFail() {
    assertThrows(WarehouseValidationException.class, () -> useCase.getById(null));
  }

  private static Warehouse archived(Warehouse warehouse) {
    warehouse.archivedAt = LocalDateTime.of(2024, 7, 1, 10, 0);
    return warehouse;
  }

  private static Warehouse warehouse(
      String businessUnitCode, String location, int capacity, int stock) {
    var warehouse = new Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }
}
