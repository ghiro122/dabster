package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;

@ApplicationScoped
public class StoreLegacySynchronizer {

  private final LegacyStoreManagerGateway legacyStoreManagerGateway;

  public StoreLegacySynchronizer(LegacyStoreManagerGateway legacyStoreManagerGateway) {
    this.legacyStoreManagerGateway = legacyStoreManagerGateway;
  }

  void synchronize(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreChangedEvent event) {
    var store = new Store();
    store.id = event.id();
    store.name = event.name();
    store.quantityProductsInStock = event.quantityProductsInStock();

    switch (event.operation()) {
      case CREATED -> legacyStoreManagerGateway.createStoreOnLegacySystem(store);
      case UPDATED -> legacyStoreManagerGateway.updateStoreOnLegacySystem(store);
    }
  }
}
