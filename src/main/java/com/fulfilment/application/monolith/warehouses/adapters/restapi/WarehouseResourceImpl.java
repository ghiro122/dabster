package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.RetrieveWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private final CreateWarehouseOperation createWarehouseOperation;
  private final RetrieveWarehouseOperation retrieveWarehouseOperation;
  private final ArchiveWarehouseOperation archiveWarehouseOperation;
  private final ReplaceWarehouseOperation replaceWarehouseOperation;

  public WarehouseResourceImpl(
      CreateWarehouseOperation createWarehouseOperation,
      RetrieveWarehouseOperation retrieveWarehouseOperation,
      ArchiveWarehouseOperation archiveWarehouseOperation,
      ReplaceWarehouseOperation replaceWarehouseOperation) {
    this.createWarehouseOperation = createWarehouseOperation;
    this.retrieveWarehouseOperation = retrieveWarehouseOperation;
    this.archiveWarehouseOperation = archiveWarehouseOperation;
    this.replaceWarehouseOperation = replaceWarehouseOperation;
  }

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return retrieveWarehouseOperation.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    var warehouse = toDomain(data);

    createWarehouseOperation.create(warehouse);

    return toWarehouseResponse(warehouse);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    return toWarehouseResponse(retrieveWarehouseOperation.getById(requireTechnicalId(id)));
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.id = requireTechnicalId(id);

    archiveWarehouseOperation.archive(warehouse);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    if (businessUnitCode == null || businessUnitCode.isBlank()) {
      throw new WarehouseValidationException("Warehouse business unit code is required.");
    }

    var warehouse = toDomain(data);
    if (warehouse.businessUnitCode != null
        && !warehouse.businessUnitCode.isBlank()
        && !warehouse.businessUnitCode.equals(businessUnitCode)) {
      throw new WarehouseValidationException(
          "The business unit code "
              + warehouse.businessUnitCode
              + " of the request body does not match "
              + businessUnitCode
              + " of the request path.");
    }
    warehouse.businessUnitCode = businessUnitCode;

    replaceWarehouseOperation.replace(warehouse);

    return toWarehouseResponse(warehouse);
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomain(
      Warehouse data) {
    if (data == null) {
      throw new WarehouseValidationException("Warehouse is required.");
    }

    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.id =
        data.getId() == null || data.getId().isBlank() ? null : parseTechnicalId(data.getId());
    warehouse.businessUnitCode = data.getBusinessUnitCode();
    warehouse.location = data.getLocation();
    warehouse.capacity = data.getCapacity();
    warehouse.stock = data.getStock();

    return warehouse;
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setId(warehouse.id == null ? null : warehouse.id.toString());
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }

  private Long requireTechnicalId(String id) {
    if (id == null || id.isBlank()) {
      throw new WarehouseValidationException("Warehouse id is required.");
    }

    return parseTechnicalId(id);
  }

  private Long parseTechnicalId(String id) {
    try {
      return Long.valueOf(id);
    } catch (NumberFormatException e) {
      throw new WarehouseValidationException("Warehouse id " + id + " is not a valid id.", e);
    }
  }
}
