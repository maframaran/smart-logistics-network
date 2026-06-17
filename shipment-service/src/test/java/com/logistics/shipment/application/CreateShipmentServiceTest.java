package com.logistics.shipment.application;

import com.logistics.shipment.application.usecases.CreateShipmentService;
import com.logistics.shipment.domain.model.*;
import com.logistics.shipment.domain.ports.in.CreateShipmentUseCase;
import com.logistics.shipment.domain.ports.out.ShipmentEventPublisher;
import com.logistics.shipment.domain.ports.out.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateShipmentServiceTest {

    @Mock ShipmentRepository repository;
    @Mock ShipmentEventPublisher eventPublisher;

    @InjectMocks CreateShipmentService service;

    private static final Address ORIGIN = new Address("1 A St", "City", "ST", "00000", "BR", 0, 0);
    private static final Address DESTINATION = new Address("2 B Ave", "Town", "ST", "11111", "BR", 1, 1);

    @Test
    void create_savesShipmentAndPublishesEvent() {
        CreateShipmentUseCase.Command command = new CreateShipmentUseCase.Command(
                "shipper-1", ORIGIN, DESTINATION,
                new CargoSpec(50.0, 1.0, false, false),
                SlaType.STANDARD,
                LocalDate.now().plusDays(5)
        );

        ShipmentId id = service.create(command);

        assertThat(id).isNotNull();
        verify(repository).save(any(Shipment.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    void create_withInvalidDate_throwsWithoutSaving() {
        CreateShipmentUseCase.Command command = new CreateShipmentUseCase.Command(
                "shipper-1", ORIGIN, DESTINATION,
                new CargoSpec(50.0, 1.0, false, false),
                SlaType.EXPRESS,
                LocalDate.now() // not in future
        );

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository, eventPublisher);
    }
}
