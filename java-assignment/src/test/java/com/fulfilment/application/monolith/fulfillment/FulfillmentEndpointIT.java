package com.fulfilment.application.monolith.fulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FulfillmentEndpointIT {

  private static final String PATH = "fulfillment";

  @Test
  @Order(1)
  public void testListFulfillments_whenEmpty_shouldReturnEmptyList() {
    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body(is("[]"));
  }

  @Test
  @Order(2)
  public void testCreateFulfillment_withValidData_shouldReturn201() {
    String body = """
        {
          "productId": 1,
          "warehouseBusinessUnitCode": "MWH.001",
          "storeId": 1
        }
        """;

    given()
        .contentType("application/json")
        .body(body)
        .when()
        .post(PATH)
        .then()
        .statusCode(201)
        .body(containsString("MWH.001"));
  }

  @Test
  @Order(3)
  public void testCreateFulfillment_withNonExistentProduct_shouldReturn404() {
    String body = """
        {
          "productId": 999,
          "warehouseBusinessUnitCode": "MWH.001",
          "storeId": 1
        }
        """;

    given()
        .contentType("application/json")
        .body(body)
        .when()
        .post(PATH)
        .then()
        .statusCode(404);
  }

  @Test
  @Order(4)
  public void testCreateFulfillment_withNonExistentStore_shouldReturn404() {
    String body = """
        {
          "productId": 1,
          "warehouseBusinessUnitCode": "MWH.001",
          "storeId": 999
        }
        """;

    given()
        .contentType("application/json")
        .body(body)
        .when()
        .post(PATH)
        .then()
        .statusCode(404);
  }

  @Test
  @Order(5)
  public void testCreateFulfillment_withNonExistentWarehouse_shouldReturn404() {
    String body = """
        {
          "productId": 1,
          "warehouseBusinessUnitCode": "NONEXISTENT",
          "storeId": 1
        }
        """;

    given()
        .contentType("application/json")
        .body(body)
        .when()
        .post(PATH)
        .then()
        .statusCode(404);
  }

  @Test
  @Order(6)
  public void testDeleteFulfillment_shouldReturn204() {
    given()
        .when()
        .delete(PATH + "/1")
        .then()
        .statusCode(204);
  }

  @Test
  @Order(7)
  public void testListFulfillments_afterDelete_shouldBeEmpty() {
    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body(is("[]"));
  }
}
