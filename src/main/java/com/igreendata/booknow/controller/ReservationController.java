package com.igreendata.booknow.controller;

import com.igreendata.booknow.configuration.ReservationConfiguration;
import com.igreendata.booknow.controller.request.BaseReservationRequest;
import com.igreendata.booknow.controller.request.CreateReservationRequest;
import com.igreendata.booknow.controller.request.UpdateReservationRequest;
import com.igreendata.booknow.domain.Reservation;
import com.igreendata.booknow.domain.ReservationTable;
import com.igreendata.booknow.domain.TimeBlock;
import com.igreendata.booknow.response.BasicReservationResponse;
import com.igreendata.booknow.response.ReservationResponse;
import com.igreendata.booknow.service.ReservationsService;
import com.igreendata.booknow.util.PropertyCopier;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(path = "/v1/reservations")
@Api(value = "Reservation Resource", description = "API for Reservation Resource")
@Slf4j
public class ReservationController {

    @Autowired
    private ReservationsService reservationsService;

    @Autowired
    private PropertyCopier propertyCopier;

    @Value("${reservation.slot.hours}")
    private String slotHours;

    @Autowired
    private ReservationConfiguration reservationConfiguration;

    @GetMapping
    @ApiOperation(value = "Returns all reservations for a specific date")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 500, message = "Internal Server Error"),
                    @ApiResponse(code = 200, message = "Successful")
            }
    )
    public List<ReservationResponse> findReservationByDate(@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        List<Reservation> allReservations = reservationsService.findByReservationDate(date);
        List<ReservationResponse> allReservationResponses = new ArrayList<>();

        for (Reservation reservation : allReservations) {

            ReservationResponse reservationResponse = new ReservationResponse();

            propertyCopier.copyProperties(reservation, reservationResponse, Set.of("id", "name", "contact"));

            reservationResponse.setTableName(reservation.getTable().getTableName());
            reservationResponse.setReservationDate(reservation.getTimeBlock().getReservationFrom().toLocalDate().toString());
            reservationResponse.setReservationTime(reservation.getTimeBlock().getReservationFrom().toLocalTime().toString());

            allReservationResponses.add(reservationResponse);
        }

        return allReservationResponses;
    }

    @GetMapping(path = "{id}")
    @ApiOperation(value = "Returns a reservation based on the id")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 404, message = "Not found"),
                    @ApiResponse(code = 500, message = "Internal Server Error"),
                    @ApiResponse(code = 200, message = "Successful")
            }
    )
    public ReservationResponse findReservationById(@PathVariable("id") Long id) {

        Optional<Reservation> returnedReservation = reservationsService.findById(id);

        if (returnedReservation.isEmpty()) {
            log.error( "Unable to find reservation");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find reservation");
        }

        List<ReservationResponse> allReservationResponses = new ArrayList<>();

        Reservation reservation = returnedReservation.get();

        ReservationResponse reservationResponse = new ReservationResponse();

        propertyCopier.copyProperties(reservation, reservationResponse, Set.of("id", "name", "contact"));

        reservationResponse.setTableName(reservation.getTable().getTableName());
        reservationResponse.setReservationDate(reservation.getTimeBlock().getReservationFrom().toLocalDate().toString());
        reservationResponse.setReservationTime(reservation.getTimeBlock().getReservationFrom().toLocalTime().toString());

        return reservationResponse;

    }

    @PostMapping
    @ApiOperation(value = "Creates a reservation")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 500, message = "Internal Server Error"),
                    @ApiResponse(code = 200, message = "Successful")
            }
    )
    public BasicReservationResponse createReservation(@RequestBody CreateReservationRequest createReservationRequest) {

        Reservation mappedReservation = mapBaseRequest(createReservationRequest);

        if(ChronoUnit.HOURS.between(mappedReservation.getTimeBlock().getReservationFrom(), mappedReservation.getTimeBlock().getReservationTo()) != Long.valueOf(slotHours)) {
            log.error("Reservations should be " + slotHours + " hours");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservations should be " + slotHours + " hours");
        }

        if(mappedReservation.getTimeBlock().getReservationFrom().toLocalTime().isBefore(reservationConfiguration.getOpeningTime())) {
            log.error("Reservation is earlier than opening time");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation is earlier than opening time");
        }

        if(mappedReservation.getTimeBlock().getReservationTo().toLocalTime().isAfter(reservationConfiguration.getClosingTime())) {
            log.error("Reservation is later than closing time");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation is later than closing time");
        }

        Reservation reservation = reservationsService.save(mappedReservation);

        BasicReservationResponse createReservationResponse = new BasicReservationResponse();
        createReservationResponse.setId(reservation.getId().toString());
        createReservationResponse.setStatus("BOOKED");

        return createReservationResponse;
    }

    @PutMapping(path = "{id}")
    @ApiOperation(value = "Updates a reservation")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 404, message = "Not found"),
                    @ApiResponse(code = 500, message = "Internal Server Error"),
                    @ApiResponse(code = 200, message = "Successful")
            }
    )
    public BasicReservationResponse updateReservation(@PathVariable("id") Long id, @RequestBody UpdateReservationRequest updateReservationRequest) {

        if(!id.equals(updateReservationRequest.getId())) {
            log.error("path variable and request body contains different ids");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "path variable and request body contains different ids");
        }

        Optional<Reservation> returnedReservation = reservationsService.findById(id);

        if(returnedReservation.isEmpty()) {
            log.error("Unable to find reservation");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find reservation");
        }

        Reservation mappedReservation = mapUpdateRequest(updateReservationRequest);

        if(mappedReservation.getTimeBlock().getReservationFrom().toLocalTime().isBefore(reservationConfiguration.getOpeningTime())) {
            log.error("Reservation is earlier than opening time");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation is earlier than opening time");
        }

        if(mappedReservation.getTimeBlock().getReservationTo().toLocalTime().isAfter(reservationConfiguration.getClosingTime())) {
            log.error("Reservation is later than closing time");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation is later than closing time");
        }

        if(ChronoUnit.HOURS.between(mappedReservation.getTimeBlock().getReservationFrom(), mappedReservation.getTimeBlock().getReservationTo()) != Long.valueOf(slotHours)) {
            log.error("Reservations should be " + slotHours + " hours");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservations should be " + slotHours + " hours");
        }

        Reservation reservation = reservationsService.save(mappedReservation);

        BasicReservationResponse createReservationResponse = new BasicReservationResponse();
        createReservationResponse.setId(reservation.getId().toString());
        createReservationResponse.setStatus("BOOKED");

        return createReservationResponse;
    }

    @DeleteMapping(path = "{id}")
    @ApiOperation(value = "Deletes reservation")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 404, message = "Not found"),
                    @ApiResponse(code = 500, message = "Internal Server Error"),
                    @ApiResponse(code = 200, message = "Successful")
            }
    )
    public BasicReservationResponse delete(@PathVariable("id") Long id) {
        reservationsService.delete(id);

        BasicReservationResponse deleteReservationResponse = new BasicReservationResponse();
        deleteReservationResponse.setId(id.toString());
        deleteReservationResponse.setStatus("UNRESERVED");

        return deleteReservationResponse;
    }

    private static Reservation mapBaseRequest(BaseReservationRequest createReservationRequest) {
        Reservation reservation = new Reservation();
        reservation.setContact(createReservationRequest.getContact());
        reservation.setName(createReservationRequest.getName());

        ReservationTable table = new ReservationTable();
        table.setTableName(createReservationRequest.getTableName());

        reservation.setTable(table);

        String[] timeRange = createReservationRequest.getReservationTime().split("-");

        DateTimeFormatter parser = DateTimeFormatter.ofPattern("h[:mm]a");
        LocalTime fromTime = LocalTime.parse(timeRange[0], parser);
        LocalTime toTime = LocalTime.parse(timeRange[1], parser);

        TimeBlock timeBlock = new TimeBlock();
        timeBlock.setReservationFrom(LocalDateTime.of(createReservationRequest.getReservationDate(), fromTime));
        timeBlock.setReservationTo(LocalDateTime.of(createReservationRequest.getReservationDate(), toTime));

        reservation.setTimeBlock(timeBlock);

        return reservation;
    }

    private Reservation mapUpdateRequest(UpdateReservationRequest updateReservationRequest) {
        Reservation reservation = mapBaseRequest(updateReservationRequest);
        reservation.setId(updateReservationRequest.getId());
        return reservation;
    }
}
