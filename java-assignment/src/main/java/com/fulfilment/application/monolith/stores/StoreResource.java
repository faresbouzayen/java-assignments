package com.fulfilment.application.monolith.stores;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.jboss.logging.Logger;

@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  @Inject EntityManager entityManager;

  private static final Logger LOGGER = Logger.getLogger(StoreResource.class.getName());

  @GET
  public List<Store> get() {
    LOGGER.debug("Listing all stores");
    return Store.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(Long id) {
    LOGGER.debugf("Finding store by id: %d", id);
    Store entity = Store.findById(id);
    if (entity == null) {
      LOGGER.warnf("Store not found: %d", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    return entity;
  }

  @POST
  @Transactional
  public Response create(Store store) {
    if (store.id != null) {
      LOGGER.warn("Create request with id set");
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    store.persist();
    entityManager.flush();

    LOGGER.infof("Store created: %s (id=%d)", store.name, store.id);
    legacyStoreManagerGateway.createStoreOnLegacySystem(store);

    return Response.ok(store).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Store update(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      LOGGER.warnf("Update request for store %d without name", id);
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      LOGGER.warnf("Store not found for update: %d", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }

    entity.name = updatedStore.name;
    entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
    entityManager.flush();

    LOGGER.infof("Store updated: %s (id=%d)", entity.name, id);
    legacyStoreManagerGateway.updateStoreOnLegacySystem(entity);

    return entity;
  }

  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(Long id, Store updatedStore) {
    Store entity = Store.findById(id);

    if (entity == null) {
      LOGGER.warnf("Store not found for patch: %d", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }

    if (updatedStore.name != null) {
      entity.name = updatedStore.name;
    }

    if (updatedStore.quantityProductsInStock != null) {
      entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
    }

    entityManager.flush();

    LOGGER.infof("Store patched: %s (id=%d)", entity.name, id);
    legacyStoreManagerGateway.updateStoreOnLegacySystem(entity);

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      LOGGER.warnf("Store not found for deletion: %d", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    entity.delete();
    LOGGER.infof("Store deleted: %s (id=%d)", entity.name, id);
    return Response.status(204).build();
  }

}
