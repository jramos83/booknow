package com.igreendata.booknow.service;

import com.igreendata.booknow.configuration.ReservationConfiguration;
import com.igreendata.booknow.domain.AvailableSlot;
import com.igreendata.booknow.domain.Reservation;
import com.igreendata.booknow.domain.ReservationTable;
import com.igreendata.booknow.domain.TableLock;
import com.igreendata.booknow.exception.TimeConflictExistsException;
import com.igreendata.booknow.repository.ReservationRepository;
import com.igreendata.booknow.repository.ReservationTableRepository;
import com.igreendata.booknow.repository.TableLockRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReservationsService {

    private ReservationRepository reservationRepository;

    private ReservationTableRepository reservationTableRepository;

    private TableLockRepository tableLockRepository;

    private ReservationConfiguration reservationConfiguration;

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
            throw new TimeConflictExistsException();
        }

        return reservationRepository.save(reservation);
    }

    public List<AvailableSlot> findAllAvailableSlots(LocalDate date) {
        List<Reservation> reservationsForDate = findByReservationDate(date);

        List<AvailableSlot> availableSlots = new ArrayList<>();

        LocalTime openingTime = reservationConfiguration.getOpeningTime();
        LocalTime closingTime = reservationConfiguration.getClosingTime();

        for(ReservationTable table: reservationTableRepository.findAll()) {
            List<Reservation> reservationsForTable = reservationsForDate.stream().filter(reservation -> reservation.getTable().getId().equals(table.getId())).collect(Collectors.toList());

            List<LocalTime> toList = new ArrayList<>();
            List<LocalTime> fromList = new ArrayList<>();
            toList.add(openingTime);

            Comparator<Reservation> reservationComparator
                    = Comparator.comparing(reservation -> reservation.getTimeBlock().getReservationFrom());

            reservationsForTable.sort(reservationComparator);

            for(Reservation reservation : reservationsForTable) {
                toList.add(reservation.getTimeBlock().getReservationTo().toLocalTime());
                fromList.add(reservation.getTimeBlock().getReservationFrom().toLocalTime());
            }

            fromList.add(closingTime);

            for(int i = 0; i < toList.size(); i ++) {
                Optional<String> gap = findTimeDifference(toList.get(i), fromList.get(i));

                if(gap.isPresent()) {
                    AvailableSlot availableSlot = new AvailableSlot();
                    availableSlot.setTableName(table.getTableName());
                    availableSlot.setAvailableDate(date);
                    availableSlot.setAvailableTime(gap.get());

                    availableSlots.add(availableSlot);
                }
            }
        }

        return availableSlots;
    }

    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    private Optional<String> findTimeDifference(LocalTime time1, LocalTime time2) {
        if(time1.equals(time2)) {
            return Optional.empty();
        } else {
            return Optional.of(time1.format(DateTimeFormatter.ofPattern("ha")) + "-" + time2.format(DateTimeFormatter.ofPattern("ha")));
        }
    }
}