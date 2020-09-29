package com.igreendata.booknow.service;

import com.igreendata.booknow.domain.ReservationTable;
import com.igreendata.booknow.repository.ReservationTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationTableService {

    @Autowired
    private ReservationTableRepository reservationTableRepository;

    public List<ReservationTable> findAllTables() {
        return reservationTableRepository.findAll();
    }
}
