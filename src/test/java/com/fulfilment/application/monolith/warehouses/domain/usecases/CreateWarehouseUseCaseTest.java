package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  private final InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();

  @Test
  public void testCreateValidWarehouse() {
    var useCase = useCaseFor(new Location("AMSTERDAM-001", 5, 100));
    var warehouse = warehouse("MWH.001", "AMSTERDAM-001", 50, 10);

    useCase.create(warehouse);

    assertEquals(1, warehouseStore.getAll().size());
    assertNotNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
    assertEquals("MWH.001", warehouse.businessUnitCode);
    assertEquals("AMSTERDAM-001", warehouse.location);
    assertEquals(50, warehouse.capacity);
    assertEquals(10, warehouse.stock);
  }

  @Test
  public void testCreateWithBusinessUnitCodeUsedByAnActiveWarehouseShouldFail() {
    var useCase = useCaseFor(new Location("AMSTERDAM-001", 5, 100));
    warehouseStore.create(warehouse("MWH.001", "AMSTERDAM-001", 20, 5));

    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.create(warehouse("MWH.001", "AMSTERDAM-001", 30, 10)));
  }

  @Test
  public void testCreateWithBusinessUnitCodeUsedOnlyByAnArchivedWarehouseShouldFail() {
    var useCase = useCaseFor(new Location("AMSTERDAM-001", 5, 100));
    var archived = warehouse("MWH.001", "AMSTERDAM-001", 20, 5);
    archived.archivedAt = LocalDateTime.now();
    warehouseStore.create(archived);

    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.create(warehouse("MWH.001", "AMSTERDAM-001", 30, 10)));
  }

  @Test
  public void testCreateOnUnknownLocationShouldFail() {
    var useCase = useCaseFor(new Location("AMSTERDAM-001", 5, 100));

    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.create(warehouse("MWH.001", "MARS-001", 30, 10)));
  }

  @Test
  public void testCreateWhenLocationAlreadyHoldsItsMaximumNumberOfWarehousesShouldFail() {
    var useCase = useCaseFor(new Location("TILBURG-001", 1, 500));
    warehouseStore.create(warehouse("MWH.023", "TILBURG-001", 30, 27));

    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.create(warehouse("MWH.001", "TILBURG-001", 30, 10)));
  }

  @Test
  public void testCreateExceedingTheAggregatedLocationCapacityShouldFail() {
    var useCase = useCaseFor(new Location("AMSTERDAM-001", 5, 100));
    warehouseStore.create(warehouse("MWH.012", "AMSTERDAM-001", 50, 5));

    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.create(warehouse("MWH.001", "AMSTERDAM-001", 60, 10)));
  }

  @Test
  public void testCreateWithStockAboveCapacityShouldFail() {
    var useCase = useCaseFor(new Location("AMSTERDAM-001", 5, 100));

    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.create(warehouse("MWH.001", "AMSTERDAM-001", 10, 20)));
  }

  @Test
  public void testCreateExactlyAtTheAggregatedLocationCapacityShouldSucceed() {
    var useCase = useCaseFor(new Location("AMSTERDAM-001", 5, 100));
    warehouseStore.create(warehouse("MWH.012", "AMSTERDAM-001", 50, 5));

    useCase.create(warehouse("MWH.001", "AMSTERDAM-001", 50, 10));

    assertEquals(2, warehouseStore.getAll().size());
  }

  private CreateWarehouseUseCase useCaseFor(Location location) {
    return new CreateWarehouseUseCase(warehouseStore, resolverFor(location));
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
