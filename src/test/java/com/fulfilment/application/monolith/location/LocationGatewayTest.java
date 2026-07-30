package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

public class LocationGatewayTest {

  private final LocationGateway locationGateway = new LocationGateway();

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    assertEquals("ZWOLLE-001", location.identification);
  }

  @Test
  public void testWhenResolveUnknownLocationShouldThrow() {
    assertThrows(
        LocationNotFoundException.class, () -> locationGateway.resolveByIdentifier("UTRECHT-999"));
  }

  @Test
  public void testWhenResolveLocationWithDifferentCaseShouldThrow() {
    assertThrows(
        LocationNotFoundException.class, () -> locationGateway.resolveByIdentifier("zwolle-001"));
  }
}
