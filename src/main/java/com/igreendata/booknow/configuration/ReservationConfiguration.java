package com.igreendata.booknow.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class ReservationConfiguration {

    @Value("${reservation.opening.time}")
    private String openingTimeString;

    @Value("${reservation.closing.time}")
    private String closingTimeString;

    @Getter
    private LocalTime openingTime;

    @Getter
    private LocalTime closingTime;

    @Getter
    private String fullDaySlot;

    @PostConstruct
    private void init() {
        openingTime = LocalTime.parse(openingTimeString, DateTimeFormatter.ofPattern("HHmm"));
        closingTime = LocalTime.parse(closingTimeString, DateTimeFormatter.ofPattern("HHmm"));
        fullDaySlot = openingTime.format(DateTimeFormatter.ofPattern("ha")) + "-" + closingTime.format(DateTimeFormatter.ofPattern("ha"));
    }

}
