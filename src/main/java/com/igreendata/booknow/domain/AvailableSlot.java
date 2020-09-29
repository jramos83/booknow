package com.igreendata.booknow.domain;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AvailableSlot {
    private String tableName;
    private LocalDate availableDate;
    private String availableTime;
}
