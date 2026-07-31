package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.LinkedHashMap;

public class WarehouseExceptionMappers {

  @Provider
  public static class ValidationMapper implements ExceptionMapper<WarehouseValidationException> {

    @Override
    public Response toResponse(WarehouseValidationException exception) {
      return toErrorResponse(exception, 400);
    }
  }

  @Provider
  public static class NotFoundMapper implements ExceptionMapper<WarehouseNotFoundException> {

    @Override
    public Response toResponse(WarehouseNotFoundException exception) {
      return toErrorResponse(exception, 404);
    }
  }

  private static Response toErrorResponse(RuntimeException exception, int code) {
    var body = new LinkedHashMap<String, Object>();
    body.put("exceptionType", exception.getClass().getName());
    body.put("code", code);
    if (exception.getMessage() != null) {
      body.put("error", exception.getMessage());
    }

    return Response.status(code).type(MediaType.APPLICATION_JSON).entity(body).build();
  }
}
