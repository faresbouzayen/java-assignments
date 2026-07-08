package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocationGatewayTest {

  private LocationGateway locationGateway;

  @BeforeEach
  public void setUp() {
    locationGateway = new LocationGateway();
  }

  @Test
  public void testResolveExistingLocationByIdentifier_shouldReturnLocation() {
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    assertNotNull(location);
    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  public void testResolveExistingLocation_amsterdam001_shouldReturnLocation() {
    Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");

    assertNotNull(location);
    assertEquals("AMSTERDAM-001", location.identification);
    assertEquals(5, location.maxNumberOfWarehouses);
    assertEquals(100, location.maxCapacity);
  }

  @Test
  public void testResolveNonExistingLocationByIdentifier_shouldReturnNull() {
    Location location = locationGateway.resolveByIdentifier("NONEXISTENT-001");

    assertNull(location);
  }

  @Test
  public void testResolveWithNullIdentifier_shouldReturnNull() {
    Location location = locationGateway.resolveByIdentifier(null);

    assertNull(location);
  }

  @Test
  public void testResolveWithEmptyIdentifier_shouldReturnNull() {
    Location location = locationGateway.resolveByIdentifier("");

    assertNull(location);
  }
}
