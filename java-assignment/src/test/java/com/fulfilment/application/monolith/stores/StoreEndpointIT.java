package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StoreEndpointIT {

  private static final String PATH = "store";

  @Test
  @Order(1)
  public void testListStores_shouldReturnAll() {
    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body(
            containsString("TONSTAD"),
            containsString("KALLAX"),
            containsString("BESTÅ"));
  }

  @Test
  @Order(2)
  public void testCreateStore_shouldReturn201() {
    String body = """
        { "name": "TEST-STORE", "quantityProductsInStock": 5 }
        """;

    given()
        .contentType("application/json")
        .body(body)
        .when()
        .post(PATH)
        .then()
        .statusCode(201)
        .body(containsString("TEST-STORE"));
  }

  @Test
  @Order(3)
  public void testGetSingleStore_shouldReturnStore() {
    given()
        .when()
        .get(PATH + "/1")
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"));
  }

  @Test
  @Order(4)
  public void testGetNonExistentStore_shouldReturn404() {
    given()
        .when()
        .get(PATH + "/999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(5)
  public void testUpdateStore_shouldReturnUpdated() {
    String body = """
        { "name": "TONSTAD-UPDATED", "quantityProductsInStock": 20 }
        """;

    given()
        .contentType("application/json")
        .body(body)
        .when()
        .put(PATH + "/1")
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD-UPDATED"));
  }

  @Test
  @Order(6)
  public void testCreateStore_withIdSet_shouldReturn422() {
    String body = """
        { "id": 99, "name": "INVALID", "quantityProductsInStock": 1 }
        """;

    given()
        .contentType("application/json")
        .body(body)
        .when()
        .post(PATH)
        .then()
        .statusCode(422);
  }

  @Test
  @Order(7)
  public void testDeleteStore_shouldReturn204() {
    given()
        .when()
        .delete(PATH + "/3")
        .then()
        .statusCode(204);
  }

  @Test
  @Order(8)
  public void testListAfterDelete_shouldExcludeDeleted() {
    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body(
            not(containsString("BESTÅ")),
            containsString("TONSTAD"),
            containsString("KALLAX"));
  }
}
