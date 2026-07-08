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

@Path("fulfillment")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfillmentResource {

  @Inject FulfillmentRepository fulfillmentRepository;

  @Inject ProductRepository productRepository;

  @Inject WarehouseRepository warehouseRepository;

  @GET
  public List<FulfillmentAssociation> list() {
    return fulfillmentRepository.listAll();
  }

  @POST
  @Transactional
  public Response create(FulfillmentAssociation association) {
    if (association.id != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    var product = productRepository.findById(association.productId);
    if (product == null) {
      throw new WebApplicationException("Product not found: " + association.productId, 404);
    }

    var store = Store.findById(association.storeId);
    if (store == null) {
      throw new WebApplicationException("Store not found: " + association.storeId, 404);
    }

    var warehouse =
        warehouseRepository.findByBusinessUnitCode(association.warehouseBusinessUnitCode);
    if (warehouse == null) {
      throw new WebApplicationException(
          "Warehouse not found: " + association.warehouseBusinessUnitCode, 404);
    }

    List<FulfillmentAssociation> productStoreAssociations =
        fulfillmentRepository.findByProductIdAndStoreId(
            association.productId, association.storeId);
    if (productStoreAssociations.size() >= 2) {
      throw new WebApplicationException(
          "Product can be fulfilled by a maximum of 2 warehouses per store", 400);
    }

    List<FulfillmentAssociation> storeAssociations =
        fulfillmentRepository.findByStoreId(association.storeId);
    if (storeAssociations.size() >= 3) {
      throw new WebApplicationException(
          "Store can be fulfilled by a maximum of 3 warehouses", 400);
    }

    List<FulfillmentAssociation> warehouseAssociations =
        fulfillmentRepository.findByWarehouseBusinessUnitCode(
            association.warehouseBusinessUnitCode);
    if (warehouseAssociations.size() >= 5) {
      throw new WebApplicationException(
          "Warehouse can store a maximum of 5 types of products", 400);
    }

    fulfillmentRepository.persist(association);
    return Response.ok(association).status(201).build();
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    var entity = fulfillmentRepository.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Fulfillment association not found: " + id, 404);
    }
    fulfillmentRepository.delete(entity);
    return Response.status(204).build();
  }
}
