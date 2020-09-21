package com.igreendata.booknow.repository;

import com.igreendata.booknow.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE FORMATDATETIME(r.timeBlock.reservationFrom,'yyyy-MM-dd') = ?1")
    public List<Reservation> findByReservationDate(LocalDate reservationDate);
}
