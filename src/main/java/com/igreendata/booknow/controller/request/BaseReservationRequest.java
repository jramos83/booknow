package com.igreendata.booknow.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BaseReservationRequest {
    private String name;
    private String contact;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reservationDate;
    private String reservationTime;
    private String tableName;
}
