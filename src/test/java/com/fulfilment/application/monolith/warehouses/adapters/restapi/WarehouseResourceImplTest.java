package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.RetrieveWarehouseOperation;
import java.util.List;
import org.junit.jupiter.api.Test;

public class WarehouseResourceImplTest {

  private final FakeOperations operations = new FakeOperations();

  private final WarehouseResourceImpl resource =
      new WarehouseResourceImpl(operations, operations, operations, operations);

  @Test
  public void testCreateShouldReturnTheIdAssignedByTheOperation() {
    var response = resource.createANewWarehouseUnit(body("MWH.001", "AMSTERDAM-001", 50, 10));

    assertEquals("7", response.getId());
    assertEquals("MWH.001", response.getBusinessUnitCode());
    assertEquals("AMSTERDAM-001", response.getLocation());
  }

  @Test
  public void testReplacementShouldUseTheBusinessUnitCodeOfThePath() {
    var data = body(null, "AMSTERDAM-001", 40, 10);

    var response = resource.replaceTheCurrentActiveWarehouse("MWH.001", data);

    assertEquals("MWH.001", operations.replaced.businessUnitCode);
    assertEquals("MWH.001", response.getBusinessUnitCode());
    assertEquals("7", response.getId());
  }

  @Test
  public void testReplacementWithABusinessUnitCodeDifferentFromThePathShouldFail() {
    var data = body("MWH.999", "AMSTERDAM-001", 40, 10);

    assertThrows(
        WarehouseValidationException.class,
        () -> resource.replaceTheCurrentActiveWarehouse("MWH.001", data));

    assertNull(operations.replaced);
  }

  @Test
  public void testNotNumericPathIdShouldFail() {
    assertThrows(
        WarehouseValidationException.class, () -> resource.getAWarehouseUnitByID("not-a-number"));
    assertThrows(
        WarehouseValidationException.class,
        () -> resource.archiveAWarehouseUnitByID("not-a-number"));

    assertNull(operations.archived);
  }

  @Test
  public void testArchiveShouldOnlyPassTheTechnicalId() {
    resource.archiveAWarehouseUnitByID("42");

    assertEquals(42L, operations.archived.id);
    assertNull(operations.archived.businessUnitCode);
    assertNull(operations.archived.location);
    assertNull(operations.archived.capacity);
    assertNull(operations.archived.stock);
  }

  private static com.warehouse.api.beans.Warehouse body(
      String businessUnitCode, String location, int capacity, int stock) {
    var data = new com.warehouse.api.beans.Warehouse();
    data.setBusinessUnitCode(businessUnitCode);
    data.setLocation(location);
    data.setCapacity(capacity);
    data.setStock(stock);
    return data;
  }

  private static class FakeOperations
      implements CreateWarehouseOperation,
          RetrieveWarehouseOperation,
          ArchiveWarehouseOperation,
          ReplaceWarehouseOperation {

    private Warehouse archived;
    private Warehouse replaced;

    @Override
    public void create(Warehouse warehouse) {
      warehouse.id = 7L;
    }

    @Override
    public void archive(Warehouse warehouse) {
      archived = warehouse;
    }

    @Override
    public void replace(Warehouse warehouse) {
      replaced = warehouse;
      warehouse.id = 7L;
    }

    @Override
    public List<Warehouse> getAll() {
      return List.of();
    }

    @Override
    public Warehouse getById(Long id) {
      throw new UnsupportedOperationException();
    }
  }
}
