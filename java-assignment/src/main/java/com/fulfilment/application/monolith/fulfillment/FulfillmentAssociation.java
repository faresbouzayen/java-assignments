package com.fulfilment.application.monolith.fulfillment;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fulfillment_association")
@Cacheable
public class FulfillmentAssociation {

  @Id @GeneratedValue public Long id;

  public Long productId;

  public String warehouseBusinessUnitCode;

  public Long storeId;

  public FulfillmentAssociation() {}

  public FulfillmentAssociation(Long productId, String warehouseBusinessUnitCode, Long storeId) {
    this.productId = productId;
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    this.storeId = storeId;
  }
}
