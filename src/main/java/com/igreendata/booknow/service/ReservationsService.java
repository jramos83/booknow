package com.igreendata.booknow.service;

import com.igreendata.booknow.domain.Reservation;
import com.igreendata.booknow.domain.ReservationTable;
import com.igreendata.booknow.domain.TableLock;
import com.igreendata.booknow.repository.ReservationRepository;
import com.igreendata.booknow.repository.ReservationTableRepository;
import com.igreendata.booknow.repository.TableLockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationsService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTableRepository reservationTableRepository;

    @Autowired
    private TableLockRepository tableLockRepository;

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> findByReservationDate(LocalDate date) {
        return reservationRepository.findByReservationDate(date);
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    public Reservation finReservations(Long id) {
        return reservationRepository.findById(id).get();
    }

    @Transactional
    public Reservation save(Reservation reservation) {

        ReservationTable table = reservationTableRepository.findByTableName(reservation.getTable().getTableName());
        reservation.setTable(table);

        TableLock tableLock = tableLockRepository.findById(reservation.getTable().getId()).get();

        List<Reservation> reservationForDate = findByReservationDate(reservation.getTimeBlock().getReservationFrom().toLocalDate());
        Optional<Reservation> conflict = reservationForDate.stream().filter(reservation1 -> (reservation.getTable().getId().equals(reservation1.getTable().getId())) && reservation.getTimeBlock().getReservationFrom().isAfter(reservation1.getTimeBlock().getReservationFrom()) && reservation.getTimeBlock().getReservationFrom().isBefore(reservation1.getTimeBlock().getReservationTo())).findAny();

        if(conflict.isPresent()) {
            throw new RuntimeException("Time conflict exists");
        }

        return reservationRepository.save(reservation);
    }

    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }
}