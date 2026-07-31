package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ResourceInfo;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;

/**
 * The OpenAPI contract documents 201 for the warehouse creation, but the JAX-RS metadata comes from
 * the generated interface, so a @ResponseStatus on the implementation is not taken into account.
 */
public class WarehouseCreatedResponseFilter {

  @ServerResponseFilter
  public void created(ContainerResponseContext response, ResourceInfo resourceInfo) {
    var resourceMethod = resourceInfo.getResourceMethod();

    if (resourceMethod != null
        && "createANewWarehouseUnit".equals(resourceMethod.getName())
        && response.getStatus() == 200) {
      response.setStatus(201);
    }
  }
}
