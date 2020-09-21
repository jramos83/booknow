package com.igreendata.booknow.response;

import lombok.Data;

@Data
public class ReservationResponse {
    private String id;
    private String tableName;
    private String reservationDate;
    private String reservationTime;
    private String name;
    private String contact;
}
