package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fulfilment.application.monolith.stores.StoreChangedEvent.Operation;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreAfterCommitTest {

  @Inject FakeLegacyStoreManagerGateway legacyStoreManagerGateway;

  @Inject Event<StoreChangedEvent> storeChangedEvent;

  @Inject UserTransaction userTransaction;

  @BeforeEach
  public void resetTheLegacySystem() {
    legacyStoreManagerGateway.reset();
  }

  @Test
  public void testTheLegacySystemIsCalledOnlyAfterTheCommit() throws Exception {
    userTransaction.begin();

    storeChangedEvent.fire(new StoreChangedEvent(Operation.CREATED, 424242L, "IN.FLIGHT", 3));

    assertEquals(
        0,
        legacyStoreManagerGateway.createCount(),
        "the legacy system must not be called while the transaction is still open");

    userTransaction.commit();

    assertEquals(1, legacyStoreManagerGateway.createCount());
    assertEquals(424242L, legacyStoreManagerGateway.lastStore().id);
  }

  @Test
  public void testTheLegacySystemIsNeverCalledOnRollback() throws Exception {
    userTransaction.begin();

    storeChangedEvent.fire(new StoreChangedEvent(Operation.CREATED, 424243L, "ROLLED.BACK", 3));

    userTransaction.rollback();

    assertEquals(0, legacyStoreManagerGateway.createCount());
    assertEquals(0, legacyStoreManagerGateway.updateCount());
  }

  @Test
  public void testCreateStoreSynchronizesTheLegacySystem() {
    int id =
        given()
            .contentType(ContentType.JSON)
            .body(storeJson("AFTERCOMMIT.CREATE", 4))
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    assertEquals(1, legacyStoreManagerGateway.createCount());
    assertEquals(0, legacyStoreManagerGateway.updateCount());

    var synchronized_ = legacyStoreManagerGateway.lastStore();
    assertEquals((long) id, synchronized_.id);
    assertEquals("AFTERCOMMIT.CREATE", synchronized_.name);
    assertEquals(4, synchronized_.quantityProductsInStock);
  }

  @Test
  public void testUpdateStoreSynchronizesTheFinalStateOfTheEntity() {
    int id = createStore("AFTERCOMMIT.UPDATE", 4);
    legacyStoreManagerGateway.reset();

    given()
        .contentType(ContentType.JSON)
        .body(storeJson("AFTERCOMMIT.UPDATED", 9))
        .when()
        .put("/store/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("AFTERCOMMIT.UPDATED"))
        .body("quantityProductsInStock", equalTo(9));

    assertEquals(1, legacyStoreManagerGateway.updateCount());
    assertEquals(0, legacyStoreManagerGateway.createCount());

    var synchronized_ = legacyStoreManagerGateway.lastStore();
    assertEquals((long) id, synchronized_.id);
    assertEquals("AFTERCOMMIT.UPDATED", synchronized_.name);
    assertEquals(9, synchronized_.quantityProductsInStock);
  }

  @Test
  public void testPatchStoreSynchronizesTheFinalStateOfTheEntity() {
    int id = createStore("AFTERCOMMIT.PATCH", 4);
    legacyStoreManagerGateway.reset();

    given()
        .contentType(ContentType.JSON)
        .body(storeJson("AFTERCOMMIT.PATCHED", 6))
        .when()
        .patch("/store/" + id)
        .then()
        .statusCode(200);

    assertEquals(1, legacyStoreManagerGateway.updateCount());
    assertEquals(0, legacyStoreManagerGateway.createCount());

    var synchronized_ = legacyStoreManagerGateway.lastStore();
    assertEquals((long) id, synchronized_.id);

    given()
        .when()
        .get("/store/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo(synchronized_.name))
        .body("quantityProductsInStock", equalTo(synchronized_.quantityProductsInStock));
  }

  private int createStore(String name, int quantityProductsInStock) {
    return given()
        .contentType(ContentType.JSON)
        .body(storeJson(name, quantityProductsInStock))
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .extract()
        .path("id");
  }

  private static String storeJson(String name, int quantityProductsInStock) {
    return """
        {
          "name": "%s",
          "quantityProductsInStock": %d
        }
        """
        .formatted(name, quantityProductsInStock);
  }
}
