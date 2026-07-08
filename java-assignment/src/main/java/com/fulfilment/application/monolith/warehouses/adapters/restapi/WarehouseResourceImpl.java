package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.jboss.logging.Logger;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private static final Logger LOGGER = Logger.getLogger(WarehouseResourceImpl.class);

  @Inject private WarehouseStore warehouseStore;

  @Inject private CreateWarehouseOperation createWarehouseOperation;

  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;

  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    LOGGER.debug("Listing all warehouses");
    return warehouseStore.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    LOGGER.debugf("Creating warehouse: %s", data.getBusinessUnitCode());
    var warehouse = toWarehouseDomain(data);
    try {
      createWarehouseOperation.create(warehouse);
    } catch (IllegalArgumentException e) {
      LOGGER.warnf("Create warehouse failed: %s", e.getMessage());
      throw new WebApplicationException(e.getMessage(), 400);
    }
    LOGGER.infof("Warehouse created: %s", warehouse.businessUnitCode);
    return toWarehouseResponse(warehouse);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    LOGGER.debugf("Finding warehouse: %s", id);
    var warehouse = warehouseStore.findByBusinessUnitCode(id);
    if (warehouse == null) {
      LOGGER.warnf("Warehouse not found: %s", id);
      throw new WebApplicationException("Warehouse not found: " + id, 404);
    }
    return toWarehouseResponse(warehouse);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    LOGGER.debugf("Archiving warehouse: %s", id);
    var warehouse = warehouseStore.findByBusinessUnitCode(id);
    if (warehouse == null) {
      LOGGER.warnf("Warehouse not found for archive: %s", id);
      throw new WebApplicationException("Warehouse not found: " + id, 404);
    }
    archiveWarehouseOperation.archive(warehouse);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    LOGGER.debugf("Replacing warehouse: %s", businessUnitCode);
    data.setBusinessUnitCode(businessUnitCode);
    var warehouse = toWarehouseDomain(data);
    try {
      replaceWarehouseOperation.replace(warehouse);
    } catch (IllegalArgumentException e) {
      LOGGER.warnf("Replace warehouse failed: %s", e.getMessage());
      throw new WebApplicationException(e.getMessage(), 400);
    }
    LOGGER.infof("Warehouse replaced: %s", businessUnitCode);
    return toWarehouseResponse(warehouse);
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toWarehouseDomain(
      Warehouse data) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = data.getBusinessUnitCode();
    warehouse.location = data.getLocation();
    warehouse.capacity = data.getCapacity();
    warehouse.stock = data.getStock();
    return warehouse;
  }
}
