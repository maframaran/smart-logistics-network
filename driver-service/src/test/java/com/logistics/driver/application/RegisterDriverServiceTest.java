package com.logistics.driver.application;

import com.logistics.driver.application.usecases.RegisterDriverService;
import com.logistics.driver.domain.model.*;
import com.logistics.driver.domain.ports.in.RegisterDriverUseCase;
import com.logistics.driver.domain.ports.out.DriverEventPublisher;
import com.logistics.driver.domain.ports.out.DriverRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterDriverServiceTest {

    @Mock DriverRepository repository;
    @Mock DriverEventPublisher eventPublisher;

    @InjectMocks RegisterDriverService service;

    @Test
    void register_savesDriverAndPublishesEvent() {
        RegisterDriverUseCase.Command command = new RegisterDriverUseCase.Command(
                "Fernanda Gomes", "LIC-FG-001", LicenseClass.C, false, "carrier-1"
        );

        DriverId id = service.register(command);

        assertThat(id).isNotNull();
        verify(repository).save(any(Driver.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    void register_withBlankName_throwsWithoutSaving() {
        RegisterDriverUseCase.Command command = new RegisterDriverUseCase.Command(
                "", "LIC-001", LicenseClass.B, false, "carrier-1"
        );

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository, eventPublisher);
    }
}
