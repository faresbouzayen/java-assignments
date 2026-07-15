package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(CreateWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    LOGGER.debugf("Creating warehouse with buCode: %s", warehouse.businessUnitCode);
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

    long totalCapacityAtLocation = warehouseStore.getAll().stream()
        .filter(w -> w.location.equals(warehouse.location) && w.archivedAt == null)
        .mapToLong(w -> w.capacity)
        .sum();
    if (totalCapacityAtLocation + warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException(
          "Total capacity at location would exceed maximum capacity: " + warehouse.location);
    }

    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Stock cannot exceed capacity");
    }

    warehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(warehouse);
    LOGGER.infof("Warehouse created: %s", warehouse.businessUnitCode);
  }
}
