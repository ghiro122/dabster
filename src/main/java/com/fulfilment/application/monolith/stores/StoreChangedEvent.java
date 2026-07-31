package com.fulfilment.application.monolith.stores;

public record StoreChangedEvent(
    Operation operation, Long id, String name, int quantityProductsInStock) {

  public enum Operation {
    CREATED,
    UPDATED
  }

  public static StoreChangedEvent created(Store store) {
    return new StoreChangedEvent(
        Operation.CREATED, store.id, store.name, store.quantityProductsInStock);
  }

  public static StoreChangedEvent updated(Store store) {
    return new StoreChangedEvent(
        Operation.UPDATED, store.id, store.name, store.quantityProductsInStock);
  }
}
