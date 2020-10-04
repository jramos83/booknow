package com.igreendata.booknow.service;

import com.igreendata.booknow.configuration.ReservationConfiguration;
import com.igreendata.booknow.domain.AvailableSlot;
import com.igreendata.booknow.domain.Reservation;
import com.igreendata.booknow.domain.ReservationTable;
import com.igreendata.booknow.domain.TimeBlock;
import com.igreendata.booknow.repository.ReservationRepository;
import com.igreendata.booknow.repository.ReservationTableRepository;
import com.igreendata.booknow.repository.TableLockRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class ReservationsServiceTest {

    @Autowired
    private ReservationsService reservationsService;

    @Mock
    private ReservationConfiguration reservationConfiguration;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTableRepository reservationTableRepository;

    @Mock
    private TableLockRepository tableLockRepository;

    @Before
    public void before() {
        this.reservationsService = new ReservationsService(reservationRepository, reservationTableRepository, tableLockRepository, reservationConfiguration);
    }

    @Test
    public void testDayWithNoBooking() {

        Mockito.when(reservationConfiguration.getOpeningTime()).thenReturn(LocalTime.of(11,00));
        Mockito.when(reservationConfiguration.getClosingTime()).thenReturn(LocalTime.of(19,00));;

        ReservationTable reservationTable1 = new ReservationTable();
        reservationTable1.setTableName("Table1");
        reservationTable1.setId(1l);

        ReservationTable reservationTable2 = new ReservationTable();
        reservationTable2.setTableName("Table2");
        reservationTable2.setId(2l);

        ReservationTable reservationTable3 = new ReservationTable();
        reservationTable3.setTableName("Table3");
        reservationTable3.setId(3l);

        List<ReservationTable> reservationTables = new ArrayList<>();
        reservationTables.add(reservationTable1);
        reservationTables.add(reservationTable2);
        reservationTables.add(reservationTable3);

        //reservations
        List<Reservation> reservations = new ArrayList<>();

        Reservation reservation1 = new Reservation();
        reservation1.setTable(reservationTable1);
        reservation1.setName("Person 1");
        reservation1.setContact("04523222");

        TimeBlock timeBlock1 = new TimeBlock();
        timeBlock1.setReservationFrom(LocalDateTime.of(LocalDate.of(2020, 02, 20), LocalTime.of(12,00)));
        timeBlock1.setReservationTo(LocalDateTime.of(LocalDate.of(2020, 02, 20), LocalTime.of(13,00)));

        reservation1.setTimeBlock(timeBlock1);
        reservations.add(reservation1);

        Reservation reservation2 = new Reservation();
        reservation2.setTable(reservationTable1);
        reservation2.setName("Person 2");
        reservation2.setContact("04523224");

        TimeBlock timeBlock2 = new TimeBlock();
        timeBlock2.setReservationFrom(LocalDateTime.of(LocalDate.of(2020, 02, 20), LocalTime.of(15,00)));
        timeBlock2.setReservationTo(LocalDateTime.of(LocalDate.of(2020, 02, 20), LocalTime.of(16,00)));

        reservation2.setTimeBlock(timeBlock2);
        reservations.add(reservation2);

        Mockito.when(reservationTableRepository.findAll()).thenReturn(reservationTables);

        Mockito.when(reservationRepository.findByReservationDate(LocalDate.of(2020, 02, 20))).thenReturn(reservations);

        LocalDate date1 = LocalDate.of(2020, 2, 20);
        List<AvailableSlot> availableSlots = reservationsService.findAllAvailableSlots(date1);

        Assert.assertEquals("11AM-7PM", availableSlots.stream().filter(availableSlot -> availableSlot.getTableName().equals("Table2")).findAny().get().getAvailableTime());

    }

    @Test
    public void testDayWithBooking() {

        Mockito.when(reservationConfiguration.getOpeningTime()).thenReturn(LocalTime.of(11,00));
        Mockito.when(reservationConfiguration.getClosingTime()).thenReturn(LocalTime.of(19,00));;

        ReservationTable reservationTable1 = new ReservationTable();
        reservationTable1.setTableName("Table1");
        reservationTable1.setId(1l);

        ReservationTable reservationTable2 = new ReservationTable();
        reservationTable2.setTableName("Table2");
        reservationTable2.setId(2l);

        ReservationTable reservationTable3 = new ReservationTable();
        reservationTable3.setTableName("Table3");
        reservationTable3.setId(3l);

        List<ReservationTable> reservationTables = new ArrayList<>();
        reservationTables.add(reservationTable1);
        reservationTables.add(reservationTable2);
        reservationTables.add(reservationTable3);

        //reservations
        List<Reservation> reservations = new ArrayList<>();

        Reservation reservation1 = new Reservation();
        reservation1.setTable(reservationTable1);
        reservation1.setName("Person 1");
        reservation1.setContact("04523222");

        TimeBlock timeBlock1 = new TimeBlock();
        timeBlock1.setReservationFrom(LocalDateTime.of(LocalDate.of(2020, 02, 20), LocalTime.of(12,00)));
        timeBlock1.setReservationTo(LocalDateTime.of(LocalDate.of(2020, 02, 20), LocalTime.of(13,00)));

        reservation1.setTimeBlock(timeBlock1);
        reservations.add(reservation1);

        Reservation reservation2 = new Reservation();
        reservation2.setTable(reservationTable1);
        reservation2.setName("Person 2");
        reservation2.setContact("04523224");

        TimeBlock timeBlock2 = new TimeBlock();
        timeBlock2.setReservationFrom(LocalDateTime.of(LocalDate.of(2020, 02, 20), LocalTime.of(15,00)));
        timeBlock2.setReservationTo(LocalDateTime.of(LocalDate.of(2020, 02, 20), LocalTime.of(16,00)));

        reservation2.setTimeBlock(timeBlock2);
        reservations.add(reservation2);

        //reservation table
        Mockito.when(reservationTableRepository.findAll()).thenReturn(reservationTables);

        Mockito.when(reservationRepository.findByReservationDate(LocalDate.of(2020, 02, 20))).thenReturn(reservations);

        //mocked
        //when then
        LocalDate date1 = LocalDate.of(2020, 2, 20);
        List<AvailableSlot> availableSlots = reservationsService.findAllAvailableSlots(date1);


        //assert full day open for dates with no reservations
        Assert.assertEquals("11AM-7PM", availableSlots.stream().filter(availableSlot -> availableSlot.getTableName().equals("Table2")).findAny().get().getAvailableTime());

        //assert gaps between reservations are returned
        List<AvailableSlot> availableSlotsForDaysWithBookings = availableSlots.stream().filter(availableSlot -> availableSlot.getTableName().equals("Table1")).collect(Collectors.toList());

        Assert.assertEquals("2020-02-20", availableSlotsForDaysWithBookings.stream().findAny().get().getAvailableDate().toString());
        Assert.assertEquals(3, availableSlotsForDaysWithBookings.size());
        Assert.assertTrue(availableSlots.stream().anyMatch(availableSlot -> availableSlot.getAvailableTime().equals("11AM-12PM")));
        Assert.assertTrue(availableSlots.stream().anyMatch(availableSlot -> availableSlot.getAvailableTime().equals("1PM-3PM")));
        Assert.assertTrue(availableSlots.stream().anyMatch(availableSlot -> availableSlot.getAvailableTime().equals("4PM-7PM")));

    }

}
