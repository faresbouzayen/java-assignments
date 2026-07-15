package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.jboss.logging.Logger;

@Path("fulfillment")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfillmentResource {

  private static final Logger LOGGER = Logger.getLogger(FulfillmentResource.class);

  @Inject FulfillmentRepository fulfillmentRepository;

  @Inject ProductRepository productRepository;

  @Inject WarehouseRepository warehouseRepository;

  @GET
  public List<FulfillmentAssociation> list() {
    LOGGER.debug("Listing all fulfillment associations");
    return fulfillmentRepository.listAll();
  }

  @POST
  @Transactional
  public Response create(FulfillmentAssociation association) {
    if (association.id != null) {
      LOGGER.warn("Create fulfillment with id set");
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    var product = productRepository.findById(association.productId);
    if (product == null) {
      LOGGER.warnf("Product not found: %d", association.productId);
      throw new WebApplicationException("Product not found: " + association.productId, 404);
    }

    var store = Store.findById(association.storeId);
    if (store == null) {
      LOGGER.warnf("Store not found: %d", association.storeId);
      throw new WebApplicationException("Store not found: " + association.storeId, 404);
    }

    var warehouse =
        warehouseRepository.findByBusinessUnitCode(association.warehouseBusinessUnitCode);
    if (warehouse == null) {
      LOGGER.warnf("Warehouse not found: %s", association.warehouseBusinessUnitCode);
      throw new WebApplicationException(
          "Warehouse not found: " + association.warehouseBusinessUnitCode, 404);
    }

    List<FulfillmentAssociation> productStoreAssociations =
        fulfillmentRepository.findByProductIdAndStoreId(
            association.productId, association.storeId);
    if (productStoreAssociations.size() >= 2) {
      LOGGER.warnf("Product %d already linked to 2 warehouses for store %d",
          association.productId, association.storeId);
      throw new WebApplicationException(
          "Product can be fulfilled by a maximum of 2 warehouses per store", 400);
    }

    List<FulfillmentAssociation> storeAssociations =
        fulfillmentRepository.findByStoreId(association.storeId);
    long distinctWarehousesForStore =
        fulfillmentRepository.countDistinctWarehousesByStoreId(association.storeId);
    if (distinctWarehousesForStore >= 3) {
      LOGGER.warnf("Store %d already linked to 3 warehouses", association.storeId);
      throw new WebApplicationException(
          "Store can be fulfilled by a maximum of 3 warehouses", 400);
    }

    List<FulfillmentAssociation> warehouseAssociations =
        fulfillmentRepository.findByWarehouseBusinessUnitCode(
            association.warehouseBusinessUnitCode);
    long distinctProductTypesForWarehouse =
        fulfillmentRepository.countDistinctProductTypesByWarehouseBusinessUnitCode(
            association.warehouseBusinessUnitCode);
    if (distinctProductTypesForWarehouse >= 5) {
      LOGGER.warnf("Warehouse %s already stores 5 product types",
          association.warehouseBusinessUnitCode);
      throw new WebApplicationException(
          "Warehouse can store a maximum of 5 types of products", 400);
    }

    fulfillmentRepository.persist(association);
    LOGGER.infof("Fulfillment created: product=%d, store=%d, warehouse=%s",
        association.productId, association.storeId, association.warehouseBusinessUnitCode);
    return Response.ok(association).status(201).build();
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    var entity = fulfillmentRepository.findById(id);
    if (entity == null) {
      LOGGER.warnf("Fulfillment not found: %d", id);
      throw new WebApplicationException("Fulfillment association not found: " + id, 404);
    }
    fulfillmentRepository.delete(entity);
    LOGGER.infof("Fulfillment deleted: %d", id);
    return Response.status(204).build();
  }
}
