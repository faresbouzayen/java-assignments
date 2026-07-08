package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  private static final Logger LOGGER = Logger.getLogger(WarehouseRepository.class);

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    var db = DbWarehouse.fromWarehouse(warehouse);
    this.persist(db);
    LOGGER.debugf("Warehouse created: %s", warehouse.businessUnitCode);
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    var db = this.find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (db != null) {
      db.location = warehouse.location;
      db.capacity = warehouse.capacity;
      db.stock = warehouse.stock;
      db.archivedAt = warehouse.archivedAt;
      this.persist(db);
      LOGGER.debugf("Warehouse updated: %s", warehouse.businessUnitCode);
    } else {
      LOGGER.warnf("Attempted to update non-existent warehouse: %s", warehouse.businessUnitCode);
    }
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    var db = this.find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (db != null) {
      this.delete(db);
      LOGGER.debugf("Warehouse removed: %s", warehouse.businessUnitCode);
    } else {
      LOGGER.warnf("Attempted to remove non-existent warehouse: %s", warehouse.businessUnitCode);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    var db = this.find("businessUnitCode", buCode).firstResult();
    return db != null ? db.toWarehouse() : null;
  }
}
