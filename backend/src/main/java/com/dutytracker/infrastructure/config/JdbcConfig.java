package com.dutytracker.infrastructure.config;

import com.dutytracker.infrastructure.persistence.converter.WorkingDaysConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;

import java.util.List;

@Configuration
public class JdbcConfig {

    @Bean
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(List.of(
                new WorkingDaysConverter.Write(),
                new WorkingDaysConverter.Read()
        ));
    }
}
