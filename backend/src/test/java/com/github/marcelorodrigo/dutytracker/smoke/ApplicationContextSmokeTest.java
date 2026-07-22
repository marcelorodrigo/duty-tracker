package com.github.marcelorodrigo.dutytracker.smoke;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.marcelorodrigo.dutytracker.DutyTrackerApplication;
import com.github.marcelorodrigo.dutytracker.gateway.holiday.JollydayPublicHolidayGateway;
import com.github.marcelorodrigo.dutytracker.gateway.holiday.PublicHolidayGateway;
import com.github.marcelorodrigo.dutytracker.testsupport.PostgreSqlContainerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class ApplicationContextSmokeTest extends PostgreSqlContainerTestSupport {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("should load the complete application context")
    void shouldLoadCompleteApplicationContext() {
        // given
        // the complete application context is started by @SpringBootTest

        // when
        var application = applicationContext.getBean(DutyTrackerApplication.class);
        var holidayGateway = applicationContext.getBean(PublicHolidayGateway.class);

        // then
        assertThat(application).isNotNull();
        assertThat(holidayGateway).isInstanceOf(JollydayPublicHolidayGateway.class);
    }
}
