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
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReplaceWarehouseUseCaseTest {

  @Mock private WarehouseStore warehouseStore;

  @Mock private LocationResolver locationResolver;

  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  public void setUp() {
    useCase = new ReplaceWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  public void testReplaceWarehouse_withValidData_shouldSucceed() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.capacity = 100;
    existing.stock = 10;
    existing.createdAt = LocalDateTime.now().minusYears(1);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "ZWOLLE-002";
    newWarehouse.capacity = 80;
    newWarehouse.stock = 10;

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    when(locationResolver.resolveByIdentifier("ZWOLLE-002"))
        .thenReturn(new Location("ZWOLLE-002", 2, 50));

    assertDoesNotThrow(() -> useCase.replace(newWarehouse));

    assertNotNull(existing.archivedAt);
    verify(warehouseStore).update(existing);
    assertNotNull(newWarehouse.createdAt);
    verify(warehouseStore).create(newWarehouse);
  }

  @Test
  public void testReplaceWarehouse_whenNoActiveWarehouse_shouldThrowException() {
    when(warehouseStore.findByBusinessUnitCode("MISSING.BUC")).thenReturn(null);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MISSING.BUC";

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(newWarehouse));
    assertEquals(
        "No active warehouse found with business unit code: MISSING.BUC", ex.getMessage());
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testReplaceWarehouse_whenExistingIsArchived_shouldThrowException() {
    Warehouse archived = new Warehouse();
    archived.businessUnitCode = "ARCH.BUC";
    archived.archivedAt = LocalDateTime.now().minusDays(1);

    when(warehouseStore.findByBusinessUnitCode("ARCH.BUC")).thenReturn(archived);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "ARCH.BUC";

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(newWarehouse));
    assertEquals(
        "No active warehouse found with business unit code: ARCH.BUC", ex.getMessage());
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testReplaceWarehouse_withInvalidLocation_shouldThrowException() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.stock = 10;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "INVALID-LOC";

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    when(locationResolver.resolveByIdentifier("INVALID-LOC")).thenReturn(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(newWarehouse));
    assertEquals("Invalid location: INVALID-LOC", ex.getMessage());
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testReplaceWarehouse_withInsufficientCapacity_shouldThrowException() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.stock = 50;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "ZWOLLE-002";
    newWarehouse.capacity = 30;
    newWarehouse.stock = 50;

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    when(locationResolver.resolveByIdentifier("ZWOLLE-002"))
        .thenReturn(new Location("ZWOLLE-002", 2, 50));

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(newWarehouse));
    assertEquals(
        "New warehouse capacity cannot accommodate the stock from the existing warehouse",
        ex.getMessage());
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testReplaceWarehouse_withStockMismatch_shouldThrowException() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.stock = 10;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "ZWOLLE-002";
    newWarehouse.capacity = 80;
    newWarehouse.stock = 20;

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    when(locationResolver.resolveByIdentifier("ZWOLLE-002"))
        .thenReturn(new Location("ZWOLLE-002", 2, 50));

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(newWarehouse));
    assertEquals(
        "New warehouse stock must match the existing warehouse stock", ex.getMessage());
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testReplaceWarehouse_withCapacityEqualToExistingStock_shouldSucceed() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.001";
    existing.location = "ZWOLLE-001";
    existing.stock = 50;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "ZWOLLE-002";
    newWarehouse.capacity = 50;
    newWarehouse.stock = 50;

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
    when(locationResolver.resolveByIdentifier("ZWOLLE-002"))
        .thenReturn(new Location("ZWOLLE-002", 2, 50));

    assertDoesNotThrow(() -> useCase.replace(newWarehouse));
    verify(warehouseStore).update(existing);
    verify(warehouseStore).create(newWarehouse);
  }
}
