package com.davidrr.grindprotocol.progression.time;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@EnableScheduling
public class DayConfiguration {
    @Bean
    public Clock businessClock(@Value("${grind.day-zone:}") String zone) {
        return Clock.system(zone.isBlank() ? ZoneId.systemDefault() : ZoneId.of(zone));
    }
}
