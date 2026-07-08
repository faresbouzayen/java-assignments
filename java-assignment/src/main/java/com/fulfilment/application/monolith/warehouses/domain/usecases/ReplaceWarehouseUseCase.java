package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    var existingWarehouse =
        warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existingWarehouse == null || existingWarehouse.archivedAt != null) {
      throw new IllegalArgumentException(
          "No active warehouse found with business unit code: " + newWarehouse.businessUnitCode);
    }

    var location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Invalid location: " + newWarehouse.location);
    }

    if (newWarehouse.capacity < existingWarehouse.stock) {
      throw new IllegalArgumentException(
          "New warehouse capacity cannot accommodate the stock from the existing warehouse");
    }

    if (!newWarehouse.stock.equals(existingWarehouse.stock)) {
      throw new IllegalArgumentException(
          "New warehouse stock must match the existing warehouse stock");
    }

    existingWarehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(existingWarehouse);

    newWarehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(newWarehouse);
  }
}
