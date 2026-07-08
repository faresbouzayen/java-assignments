package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseEndpointIT {

  private static final String PATH = "warehouse";

  @Test
  @Order(1)
  public void testSimpleListWarehouses() {
    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body(
            containsString("MWH.001"),
            containsString("MWH.012"),
            containsString("MWH.023"),
            containsString("ZWOLLE-001"),
            containsString("AMSTERDAM-001"),
            containsString("TILBURG-001"));
  }

  @Test
  @Order(2)
  public void testArchiveWarehouseByBusinessUnitCode_shouldReturn204() {
    given().when().delete(PATH + "/MWH.001").then().statusCode(204);
  }

  @Test
  @Order(3)
  public void testListAfterArchive_archivedWarehouseShouldBeExcluded() {
    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body(
            not(containsString("ZWOLLE-001")),
            containsString("AMSTERDAM-001"),
            containsString("TILBURG-001"));
  }

  @Test
  @Order(4)
  public void testArchiveNonExistentWarehouse_shouldReturn404() {
    given().when().delete(PATH + "/NONEXISTENT").then().statusCode(404);
  }
}
