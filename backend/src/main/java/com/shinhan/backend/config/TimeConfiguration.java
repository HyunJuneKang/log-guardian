package com.shinhan.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfiguration {
    public static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Bean
    Clock applicationClock() {
        return Clock.system(SEOUL);
    }
}
