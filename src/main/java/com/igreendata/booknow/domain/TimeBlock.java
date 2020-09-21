package com.igreendata.booknow.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
@Data
public class TimeBlock {
    @Column(name = "reservation_from")
    private LocalDateTime reservationFrom;
    @Column(name = "reservation_to")
    private LocalDateTime reservationTo;
}
