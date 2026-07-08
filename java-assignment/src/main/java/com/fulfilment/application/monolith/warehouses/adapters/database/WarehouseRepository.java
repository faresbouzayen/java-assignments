package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    var db = DbWarehouse.fromWarehouse(warehouse);
    this.persist(db);
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
    }
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    var db = this.find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (db != null) {
      this.delete(db);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    var db = this.find("businessUnitCode", buCode).firstResult();
    return db != null ? db.toWarehouse() : null;
  }
}
