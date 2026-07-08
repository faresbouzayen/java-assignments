package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArchiveWarehouseUseCaseTest {

  @Mock private WarehouseStore warehouseStore;

  private ArchiveWarehouseUseCase useCase;

  @BeforeEach
  public void setUp() {
    useCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  public void testArchiveWarehouse_shouldSetArchivedAtTimestamp() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";

    useCase.archive(warehouse);

    assertNotNull(warehouse.archivedAt);
    verify(warehouseStore).update(warehouse);
  }

  @Test
  public void testArchiveWarehouse_alreadyArchived_shouldUpdateArchivedAt() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.archivedAt = java.time.LocalDateTime.now().minusDays(1);

    useCase.archive(warehouse);

    assertNotNull(warehouse.archivedAt);
    verify(warehouseStore).update(warehouse);
  }
}
