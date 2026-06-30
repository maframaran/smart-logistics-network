package com.logistics.driver.domain;

import com.logistics.driver.domain.events.DriverRegistered;
import com.logistics.driver.domain.events.DriverStatusChanged;
import com.logistics.driver.domain.model.*;
import com.logistics.common.domain.DomainEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DriverTest {

    private static final LocalDate TODAY = LocalDate.now();

    @Test
    void register_raisesDriverRegisteredEvent() {
        Driver driver = Driver.register("João Silva", "LIC-001", LicenseClass.C, false, "carrier-1");

        List<DomainEvent> events = driver.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(DriverRegistered.class);
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.AVAILABLE);
    }

    @Test
    void register_withBlankName_throws() {
        assertThatThrownBy(() -> Driver.register("", "LIC-001", LicenseClass.B, false, "carrier-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fullName");
    }

    @Test
    void updateStatus_raisesStatusChangedEvent() {
        Driver driver = Driver.register("Ana Costa", "LIC-002", LicenseClass.E, true, "carrier-1");
        driver.pullDomainEvents();

        driver.updateStatus(DriverStatus.DRIVING, "Started delivery");

        assertThat(driver.getStatus()).isEqualTo(DriverStatus.DRIVING);
        DriverStatusChanged event = (DriverStatusChanged) driver.pullDomainEvents().getFirst();
        assertThat(event.previousStatus()).isEqualTo(DriverStatus.AVAILABLE);
        assertThat(event.newStatus()).isEqualTo(DriverStatus.DRIVING);
    }

    @Test
    void recordDriving_withinLimit_succeeds() {
        Driver driver = Driver.register("Carlos Lima", "LIC-003", LicenseClass.C, false, "carrier-2");

        assertThatCode(() -> driver.recordDriving(TODAY, Duration.ofHours(8)))
                .doesNotThrowAnyException();
        assertThat(driver.getDrivingSessionFor(TODAY).hoursWorked()).isEqualTo(Duration.ofHours(8));
    }

    @Test
    void recordDriving_exceedsNineHourLimit_throws() {
        Driver driver = Driver.register("Maria Souza", "LIC-004", LicenseClass.D, false, "carrier-2");
        driver.recordDriving(TODAY, Duration.ofHours(8));

        Duration twoHours = Duration.ofHours(2);
        assertThatThrownBy(() -> driver.recordDriving(TODAY, twoHours))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("9h daily limit");
    }

    @Test
    void recordDriving_onDifferentDays_tracksIndependently() {
        Driver driver = Driver.register("Pedro Rocha", "LIC-005", LicenseClass.C, false, "c1");
        LocalDate yesterday = TODAY.minusDays(1);

        driver.recordDriving(yesterday, Duration.ofHours(9));
        assertThatCode(() -> driver.recordDriving(TODAY, Duration.ofHours(9)))
                .doesNotThrowAnyException();
    }

    @Test
    void hazmatCertified_driverCanDriveHazmat() {
        Driver certified = Driver.register("Expert Driver", "LIC-HAZ", LicenseClass.E, true, "c1");
        Driver regular = Driver.register("Regular Driver", "LIC-REG", LicenseClass.C, false, "c1");

        assertThat(certified.canDriveHazmat()).isTrue();
        assertThat(regular.canDriveHazmat()).isFalse();
    }

    @Test
    void updateStatus_toSameStatus_throws() {
        Driver driver = Driver.register("Test Driver", "LIC-007", LicenseClass.B, false, "c1");
        assertThatThrownBy(() -> driver.updateStatus(DriverStatus.AVAILABLE, "same"))
                .isInstanceOf(IllegalStateException.class);
    }
}
