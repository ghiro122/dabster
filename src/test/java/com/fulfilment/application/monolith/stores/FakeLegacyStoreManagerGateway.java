package com.fulfilment.application.monolith.stores;

import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;

/** Records the legacy calls instead of writing files, and never touches the database. */
@Mock
@ApplicationScoped
public class FakeLegacyStoreManagerGateway extends LegacyStoreManagerGateway {

  private int createCount;

  private int updateCount;

  private Store lastStore;

  @Override
  public void createStoreOnLegacySystem(Store store) {
    createCount++;
    lastStore = store;
  }

  @Override
  public void updateStoreOnLegacySystem(Store store) {
    updateCount++;
    lastStore = store;
  }

  public int createCount() {
    return createCount;
  }

  public int updateCount() {
    return updateCount;
  }

  public Store lastStore() {
    return lastStore;
  }

  public void reset() {
    createCount = 0;
    updateCount = 0;
    lastStore = null;
  }
}
