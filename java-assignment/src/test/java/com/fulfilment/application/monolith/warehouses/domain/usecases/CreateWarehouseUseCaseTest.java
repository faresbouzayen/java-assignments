package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CreateWarehouseUseCaseTest {

  @Mock private WarehouseStore warehouseStore;

  @Mock private LocationResolver locationResolver;

  private CreateWarehouseUseCase useCase;

  @BeforeEach
  public void setUp() {
    useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  public void testCreateWarehouse_withValidData_shouldSucceed() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "NEW.BUC";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 30;
    warehouse.stock = 10;

    when(warehouseStore.findByBusinessUnitCode("NEW.BUC")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));
    when(warehouseStore.getAll()).thenReturn(List.of());

    assertDoesNotThrow(() -> useCase.create(warehouse));

    assertNotNull(warehouse.createdAt);
    verify(warehouseStore).create(warehouse);
  }

  @Test
  public void testCreateWarehouse_withDuplicateBusinessUnitCode_shouldThrowException() {
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "EXIST.BUC";
    existingWarehouse.archivedAt = null;

    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "EXIST.BUC";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 30;
    warehouse.stock = 10;

    when(warehouseStore.findByBusinessUnitCode("EXIST.BUC")).thenReturn(existingWarehouse);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Business unit code already exists: EXIST.BUC", ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouse_withPreviouslyArchivedBuc_shouldSucceed() {
    Warehouse archivedWarehouse = new Warehouse();
    archivedWarehouse.businessUnitCode = "ARCH.BUC";
    archivedWarehouse.archivedAt = java.time.LocalDateTime.now().minusDays(1);

    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "ARCH.BUC";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 30;
    warehouse.stock = 10;

    when(warehouseStore.findByBusinessUnitCode("ARCH.BUC")).thenReturn(archivedWarehouse);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));
    when(warehouseStore.getAll()).thenReturn(List.of());

    assertDoesNotThrow(() -> useCase.create(warehouse));
    verify(warehouseStore).create(warehouse);
  }

  @Test
  public void testCreateWarehouse_withInvalidLocation_shouldThrowException() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "NEW.BUC";
    warehouse.location = "INVALID-LOC";
    warehouse.capacity = 30;
    warehouse.stock = 10;

    when(warehouseStore.findByBusinessUnitCode("NEW.BUC")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("INVALID-LOC")).thenReturn(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Invalid location: INVALID-LOC", ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouse_whenMaxWarehousesReached_shouldThrowException() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "NEW.BUC";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 30;
    warehouse.stock = 10;

    Warehouse existingAtLocation = new Warehouse();
    existingAtLocation.location = "ZWOLLE-001";
    existingAtLocation.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("NEW.BUC")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));
    when(warehouseStore.getAll()).thenReturn(List.of(existingAtLocation));

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Maximum number of warehouses reached at location: ZWOLLE-001", ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouse_withCapacityExceedingLocationMax_shouldThrowException() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "NEW.BUC";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 50;
    warehouse.stock = 10;

    when(warehouseStore.findByBusinessUnitCode("NEW.BUC")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));
    when(warehouseStore.getAll()).thenReturn(List.of());

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals(
        "Capacity exceeds maximum capacity for location: ZWOLLE-001", ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouse_withStockExceedingCapacity_shouldThrowException() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "NEW.BUC";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 30;
    warehouse.stock = 50;

    when(warehouseStore.findByBusinessUnitCode("NEW.BUC")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));
    when(warehouseStore.getAll()).thenReturn(List.of());

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    assertEquals("Stock cannot exceed capacity", ex.getMessage());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouse_withStockEqualToCapacity_shouldSucceed() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "NEW.BUC";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 30;
    warehouse.stock = 30;

    when(warehouseStore.findByBusinessUnitCode("NEW.BUC")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));
    when(warehouseStore.getAll()).thenReturn(List.of());

    assertDoesNotThrow(() -> useCase.create(warehouse));
    verify(warehouseStore).create(warehouse);
  }

  @Test
  public void testCreateWarehouse_withArchivedWarehouseNotCountingTowardsMax_shouldSucceed() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "NEW.BUC";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 30;
    warehouse.stock = 10;

    Warehouse archivedAtLocation = new Warehouse();
    archivedAtLocation.location = "ZWOLLE-001";
    archivedAtLocation.archivedAt = java.time.LocalDateTime.now().minusDays(1);

    when(warehouseStore.findByBusinessUnitCode("NEW.BUC")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));
    when(warehouseStore.getAll()).thenReturn(List.of(archivedAtLocation));

    assertDoesNotThrow(() -> useCase.create(warehouse));
    verify(warehouseStore).create(warehouse);
  }
}
