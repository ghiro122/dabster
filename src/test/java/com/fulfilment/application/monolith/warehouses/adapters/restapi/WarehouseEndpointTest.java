package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseEndpointTest {

  @Test
  public void testListAllWarehousesUnits() {
    given()
        .when()
        .get("/warehouse")
        .then()
        .statusCode(200)
        .body("businessUnitCode", hasItem("MWH.001"))
        .body("find { it.businessUnitCode == 'MWH.001' }.id", notNullValue());
  }

  @Test
  public void testCreateAValidWarehouseUnit() {
    String id =
        given()
            .contentType(ContentType.JSON)
            .body(warehouseJson("TEST.CREATE.001", "AMSTERDAM-002", 10, 5))
            .when()
            .post("/warehouse")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("businessUnitCode", equalTo("TEST.CREATE.001"))
            .body("location", equalTo("AMSTERDAM-002"))
            .body("capacity", equalTo(10))
            .body("stock", equalTo(5))
            .extract()
            .path("id");

    assertTrue(id.matches("\\d+"), "the returned id should be numeric but was " + id);

    given()
        .when()
        .get("/warehouse/" + id)
        .then()
        .statusCode(200)
        .body("id", equalTo(id))
        .body("businessUnitCode", equalTo("TEST.CREATE.001"))
        .body("location", equalTo("AMSTERDAM-002"))
        .body("capacity", equalTo(10))
        .body("stock", equalTo(5));
  }

  @Test
  public void testCreateAWarehouseUnitWithMoreStockThanCapacity() {
    given()
        .contentType(ContentType.JSON)
        .body(warehouseJson("TEST.INVALID.001", "AMSTERDAM-002", 5, 10))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400)
        .body("code", equalTo(400))
        .body("error", notNullValue());

    assertEquals(0, countActiveOccurrences("TEST.INVALID.001"));
  }

  @Test
  public void testGetAWarehouseUnitByANotNumericID() {
    given()
        .when()
        .get("/warehouse/not-a-number")
        .then()
        .statusCode(400)
        .body("code", equalTo(400))
        .body("error", notNullValue());
  }

  @Test
  public void testGetAWarehouseUnitByAnUnknownID() {
    given()
        .when()
        .get("/warehouse/999999")
        .then()
        .statusCode(404)
        .body("code", equalTo(404))
        .body("error", notNullValue());
  }

  @Test
  public void testArchiveAWarehouseUnitIsIdempotent() {
    String id = createWarehouse("TEST.ARCHIVE.001", "EINDHOVEN-001", 10, 5);

    given().when().delete("/warehouse/" + id).then().statusCode(204);
    given().when().delete("/warehouse/" + id).then().statusCode(204);

    given().when().get("/warehouse/" + id).then().statusCode(404);

    assertEquals(0, countActiveOccurrences("TEST.ARCHIVE.001"));
  }

  @Test
  public void testReplaceTheCurrentActiveWarehouse() {
    String replacedId = createWarehouse("TEST.REPLACE.001", "VETSBY-001", 20, 5);

    String replacingId =
        given()
            .contentType(ContentType.JSON)
            .body(warehouseJson("TEST.REPLACE.001", "VETSBY-001", 30, 5))
            .when()
            .post("/warehouse/TEST.REPLACE.001/replacement")
            .then()
            .statusCode(200)
            .body("businessUnitCode", equalTo("TEST.REPLACE.001"))
            .body("capacity", equalTo(30))
            .body("stock", equalTo(5))
            .extract()
            .path("id");

    assertNotEquals(replacedId, replacingId);

    given().when().get("/warehouse/" + replacedId).then().statusCode(404);
    given().when().get("/warehouse/" + replacingId).then().statusCode(200);

    assertEquals(1, countActiveOccurrences("TEST.REPLACE.001"));
  }

  @Test
  public void testReplaceWithABusinessUnitCodeDifferentFromThePath() {
    String id = createWarehouse("TEST.MISMATCH.001", "ZWOLLE-002", 10, 5);

    given()
        .contentType(ContentType.JSON)
        .body(warehouseJson("TEST.MISMATCH.999", "ZWOLLE-002", 10, 5))
        .when()
        .post("/warehouse/TEST.MISMATCH.001/replacement")
        .then()
        .statusCode(400)
        .body("code", equalTo(400))
        .body("error", notNullValue());

    given()
        .when()
        .get("/warehouse/" + id)
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo("TEST.MISMATCH.001"))
        .body("capacity", equalTo(10));
  }

  private String createWarehouse(
      String businessUnitCode, String location, int capacity, int stock) {
    return given()
        .contentType(ContentType.JSON)
        .body(warehouseJson(businessUnitCode, location, capacity, stock))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(201)
        .extract()
        .path("id");
  }

  private long countActiveOccurrences(String businessUnitCode) {
    List<String> businessUnitCodes =
        given()
            .when()
            .get("/warehouse")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList("businessUnitCode", String.class);

    return businessUnitCodes.stream().filter(businessUnitCode::equals).count();
  }

  private static String warehouseJson(
      String businessUnitCode, String location, int capacity, int stock) {
    return """
        {
          "businessUnitCode": "%s",
          "location": "%s",
          "capacity": %d,
          "stock": %d
        }
        """
        .formatted(businessUnitCode, location, capacity, stock);
  }
}
