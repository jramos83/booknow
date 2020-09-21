package com.igreendata.booknow.domain;

import lombok.Data;

import javax.persistence.*;

@Table(name = "reservation", uniqueConstraints = @UniqueConstraint(columnNames = { "table_id","reservation_from", "reservation_to" }))
@Entity
@Data
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "table_id")
    private ReservationTable table;

    @Embedded
    private TimeBlock timeBlock;

    @Column
    private String name;

    @Column
    private String contact;

}
