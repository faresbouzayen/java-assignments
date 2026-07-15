package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FulfillmentRepository implements PanacheRepository<FulfillmentAssociation> {

  public List<FulfillmentAssociation> findByProductIdAndStoreId(Long productId, Long storeId) {
    return list("productId = ?1 and storeId = ?2", productId, storeId);
  }

  public List<FulfillmentAssociation> findByStoreId(Long storeId) {
    return list("storeId", storeId);
  }

  public List<FulfillmentAssociation> findByWarehouseBusinessUnitCode(String warehouseBuCode) {
    return list("warehouseBusinessUnitCode", warehouseBuCode);
  }

  public long countDistinctWarehousesByStoreId(Long storeId) {
    return list("storeId", storeId).stream()
        .map(a -> a.warehouseBusinessUnitCode)
        .distinct()
        .count();
  }

  public long countDistinctProductTypesByWarehouseBusinessUnitCode(String warehouseBuCode) {
    return list("warehouseBusinessUnitCode", warehouseBuCode).stream()
        .map(a -> a.productId)
        .distinct()
        .count();
  }
}
