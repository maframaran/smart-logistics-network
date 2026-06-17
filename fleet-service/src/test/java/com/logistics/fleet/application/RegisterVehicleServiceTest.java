package com.logistics.fleet.application;

import com.logistics.fleet.application.usecases.RegisterVehicleService;
import com.logistics.fleet.domain.model.*;
import com.logistics.fleet.domain.ports.in.RegisterVehicleUseCase;
import com.logistics.fleet.domain.ports.out.VehicleEventPublisher;
import com.logistics.fleet.domain.ports.out.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterVehicleServiceTest {

    @Mock VehicleRepository repository;
    @Mock VehicleEventPublisher eventPublisher;

    @InjectMocks RegisterVehicleService service;

    @Test
    void register_savesVehicleAndPublishesEvent() {
        RegisterVehicleUseCase.Command command = new RegisterVehicleUseCase.Command(
                "ABC-1234", VehicleType.TRUCK, new Capacity(5000, 20), "carrier-1"
        );

        VehicleId id = service.register(command);

        assertThat(id).isNotNull();
        verify(repository).save(any(Vehicle.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    void register_withBlankPlate_throwsWithoutSaving() {
        RegisterVehicleUseCase.Command command = new RegisterVehicleUseCase.Command(
                "", VehicleType.VAN, new Capacity(1000, 5), "carrier-1"
        );

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository, eventPublisher);
    }
}
