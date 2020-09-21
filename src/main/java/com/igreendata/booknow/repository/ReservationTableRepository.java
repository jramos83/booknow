package com.igreendata.booknow.repository;

import com.igreendata.booknow.domain.ReservationTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTableRepository extends JpaRepository<ReservationTable, Long> {

    public ReservationTable findByTableName(String tableName);
}
