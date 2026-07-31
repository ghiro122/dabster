package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fulfilment.application.monolith.stores.StoreChangedEvent.Operation;
import org.junit.jupiter.api.Test;

public class StoreLegacySynchronizerTest {

  private final FakeLegacyStoreManagerGateway gateway = new FakeLegacyStoreManagerGateway();

  private final StoreLegacySynchronizer synchronizer = new StoreLegacySynchronizer(gateway);

  @Test
  public void testCreatedEventShouldOnlyCallCreateOnTheLegacySystem() {
    synchronizer.synchronize(new StoreChangedEvent(Operation.CREATED, 1L, "TONSTAD", 10));

    assertEquals(1, gateway.createCount());
    assertEquals(0, gateway.updateCount());
  }

  @Test
  public void testUpdatedEventShouldOnlyCallUpdateOnTheLegacySystem() {
    synchronizer.synchronize(new StoreChangedEvent(Operation.UPDATED, 1L, "TONSTAD", 10));

    assertEquals(1, gateway.updateCount());
    assertEquals(0, gateway.createCount());
  }

  @Test
  public void testTheLegacySystemShouldReceiveTheSnapshotValues() {
    synchronizer.synchronize(new StoreChangedEvent(Operation.UPDATED, 42L, "KALLAX", 7));

    var received = gateway.lastStore();
    assertEquals(42L, received.id);
    assertEquals("KALLAX", received.name);
    assertEquals(7, received.quantityProductsInStock);
  }
}
