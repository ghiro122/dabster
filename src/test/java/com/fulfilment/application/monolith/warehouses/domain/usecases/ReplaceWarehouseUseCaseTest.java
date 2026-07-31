package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  private final InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();

  @Test
  public void testReplaceActiveWarehouse() {
    var useCase = useCaseFor(new Location("AMSTERDAM-001", 5, 100));
    var replaced = warehouse("MWH.001", "AMSTERDAM-001", 50, 10);
    warehouseStore.seed(replaced);

    var replacing = warehouse("MWH.001", "AMSTERDAM-001", 40, 10);
    useCase.replace(replacing);

    var archived = warehouseStore.findByTechnicalId(replaced.id);
    assertNotNull(archived);
    assertNotNull(archived.archivedAt);

    var current = warehouseStore.findByBusinessUnitCode("MWH.001");
    assertNotNull(current);
    assertEquals("MWH.001", current.businessUnitCode);
    assertNotEquals(replaced.id, current.id);
    assertEquals(10, current.stock);
    assertEquals(40, current.capacity);
    assertNotNull(current.createdAt);
    assertNull(current.archivedAt);

    assertEquals(1, activeWarehousesWithCode("MWH.001"));
    assertEquals(1, warehouseStore.updateCount());
    assertEquals(1, warehouseStore.createCount());
  }

  @Test
  public void testReplaceUnknownBusinessUnitCodeShouldFail() {
    var useCase = useCaseFor(new Location("AMSTERDAM-001", 5, 100));

    assertThrows(
        WarehouseNotFoundException.class,
        () -> useCase.replace(warehouse("MWH.999", "AMSTERDAM-001", 40, 10)));

    assertNoWriteHappened();
  }

  @Test
  public void testReplaceWithADifferentStockShouldFail() {
    var useCase = useCaseFor(new Location("AMSTERDAM-001", 5, 100));
    var replaced = warehouse("MWH.001", "AMSTERDAM-001", 50, 10);
    warehouseStore.seed(replaced);

    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.replace(warehouse("MWH.001", "AMSTERDAM-001", 40, 20)));

    assertNull(warehouseStore.findByTechnicalId(replaced.id).archivedAt);
    assertNoWriteHappened();
  }

  @Test
  public void testReplaceWithACapacityBelowThePreviousStockShouldFail() {
    var useCase = useCaseFor(new Location("AMSTERDAM-001", 5, 100));
    var replaced = warehouse("MWH.001", "AMSTERDAM-001", 50, 10);
    warehouseStore.seed(replaced);

    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.replace(warehouse("MWH.001", "AMSTERDAM-001", 5, 10)));

    assertNull(warehouseStore.findByTechnicalId(replaced.id).archivedAt);
    assertNoWriteHappened();
  }

  @Test
  public void testReplaceOnUnknownLocationShouldFail() {
    var useCase = useCaseFor(new Location("AMSTERDAM-001", 5, 100));
    var replaced = warehouse("MWH.001", "AMSTERDAM-001", 50, 10);
    warehouseStore.seed(replaced);

    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.replace(warehouse("MWH.001", "MARS-001", 40, 10)));

    assertNull(warehouseStore.findByTechnicalId(replaced.id).archivedAt);
    assertNoWriteHappened();
  }

  @Test
  public void testReplaceExcludesTheReplacedWarehouseFromTheLocationLimits() {
    // ZWOLLE-001 holds a single warehouse and 40 of capacity, while the warehouse being replaced
    // already takes both. The replacement can only succeed if that warehouse is left out.
    var useCase = useCaseFor(new Location("ZWOLLE-001", 1, 40));
    var replaced = warehouse("MWH.001", "ZWOLLE-001", 100, 10);
    warehouseStore.seed(replaced);

    useCase.replace(warehouse("MWH.001", "ZWOLLE-001", 30, 10));

    assertEquals(1, activeWarehousesWithCode("MWH.001"));
    assertNotNull(warehouseStore.findByTechnicalId(replaced.id).archivedAt);
    assertEquals(30, warehouseStore.findByBusinessUnitCode("MWH.001").capacity);
  }

  @Test
  public void testReplaceValidationFailureShouldHappenBeforeAnyWrite() {
    var useCase = useCaseFor(new Location("AMSTERDAM-001", 5, 100));
    var replaced = warehouse("MWH.001", "AMSTERDAM-001", 50, 10);
    warehouseStore.seed(replaced);

    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.replace(warehouse("MWH.001", "AMSTERDAM-001", 0, 0)));

    assertNull(warehouseStore.findByTechnicalId(replaced.id).archivedAt);
    assertEquals(0, warehouseStore.updateCount());
    assertEquals(0, warehouseStore.createCount());
  }

  private void assertNoWriteHappened() {
    assertEquals(0, warehouseStore.updateCount());
    assertEquals(0, warehouseStore.createCount());
  }

  private long activeWarehousesWithCode(String businessUnitCode) {
    return warehouseStore.getAll().stream()
        .filter(warehouse -> warehouse.businessUnitCode.equals(businessUnitCode))
        .count();
  }

  private ReplaceWarehouseUseCase useCaseFor(Location location) {
    return new ReplaceWarehouseUseCase(warehouseStore, resolverFor(location));
  }

  private static LocationResolver resolverFor(Location location) {
    return identifier -> {
      if (location.identification.equals(identifier)) {
        return location;
      }
      throw new LocationNotFoundException(identifier);
    };
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
