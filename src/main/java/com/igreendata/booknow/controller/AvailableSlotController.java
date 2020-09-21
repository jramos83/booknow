package com.igreendata.booknow.controller;

import com.igreendata.booknow.configuration.ReservationConfiguration;
import com.igreendata.booknow.domain.Reservation;
import com.igreendata.booknow.domain.ReservationTable;
import com.igreendata.booknow.response.AvailableSlotResponse;
import com.igreendata.booknow.service.ReservationTableService;
import com.igreendata.booknow.service.ReservationsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/v1/availableSlots")
@Api(value = "Reservation Resource", description = "API for Reservation Resource")
@Slf4j
public class AvailableSlotController {

    @Autowired
    private ReservationTableService reservationTableService;

    @Autowired
    private ReservationsService reservationsService;

    @Autowired
    private ReservationConfiguration reservationConfiguration;

    @GetMapping
    @ApiOperation(value = "Get available slots for a given date")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 500, message = "Internal Server Error"),
                    @ApiResponse(code = 200, message = "Successful")
            }
    )
    public List<AvailableSlotResponse> findAllAvailableSlots(@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        List<Reservation> reservationsForDate = reservationsService.findByReservationDate(date);

        List<AvailableSlotResponse> availableSlotResponses = new ArrayList<>();

        LocalTime openingTime = reservationConfiguration.getOpeningTime();
        LocalTime closingTime = reservationConfiguration.getClosingTime();

        for(ReservationTable table: reservationTableService.findAllTables()) {
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
                    AvailableSlotResponse availableSlotResponse = new AvailableSlotResponse();
                    availableSlotResponse.setTableName(table.getTableName());
                    availableSlotResponse.setAvailableDate(date);
                    availableSlotResponse.setAvailableTime(gap.get());

                    availableSlotResponses.add(availableSlotResponse);
                }
            }
        }

        return availableSlotResponses;
    }

    private Optional<String> findTimeDifference(LocalTime time1, LocalTime time2) {
        if(time1.equals(time2)) {
            return Optional.empty();
        } else {
            return Optional.of(time1.format(DateTimeFormatter.ofPattern("ha")) + "-" + time2.format(DateTimeFormatter.ofPattern("ha")));
        }
    }
}
