package com.igreendata.booknow.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AvailableSlotResponse {
    private String tableName;
    private LocalDate availableDate;
    private String availableTime;
}
