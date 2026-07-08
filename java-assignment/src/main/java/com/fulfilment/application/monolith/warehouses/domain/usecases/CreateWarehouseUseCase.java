package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    var existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing != null && existing.archivedAt == null) {
      throw new IllegalArgumentException(
          "Business unit code already exists: " + warehouse.businessUnitCode);
    }

    var location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Invalid location: " + warehouse.location);
    }

    long activeWarehouseCount = warehouseStore.getAll().stream()
        .filter(w -> w.location.equals(warehouse.location) && w.archivedAt == null)
        .count();
    if (activeWarehouseCount >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException(
          "Maximum number of warehouses reached at location: " + warehouse.location);
    }

    if (warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException(
          "Capacity exceeds maximum capacity for location: " + warehouse.location);
    }

    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Stock cannot exceed capacity");
    }

    warehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(warehouse);
  }
}
