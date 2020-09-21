package com.igreendata.booknow.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class ReservationTable {

    @Id
    private Long id;

    @Column(name = "table_name")
    private String tableName;

}
