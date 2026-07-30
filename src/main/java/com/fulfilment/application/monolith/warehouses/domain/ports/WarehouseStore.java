package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.util.List;

public interface WarehouseStore {

  /** 
   * Only the active warehouses: archived ones are excluded 
  */
  List<Warehouse> getAll();

  void create(Warehouse warehouse);

  void update(Warehouse warehouse);

  void remove(Warehouse warehouse);

  /** 
   * Only the active warehouse holding that code, or null: archived ones are excluded. 
  */
  Warehouse findByBusinessUnitCode(String buCode);

  /**
   *  Whether that code was ever used, by an active or by an archived warehouse
  */
  boolean existsByBusinessUnitCode(String buCode);

  /** 
   * The record with that technical id, or null: archived ones are included
  */
  Warehouse findByTechnicalId(Long id);

  List<Warehouse> findActiveByLocation(String locationIdentifier);
}
