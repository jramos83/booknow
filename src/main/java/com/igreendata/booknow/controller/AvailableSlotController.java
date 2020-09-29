package com.igreendata.booknow.controller;

import com.igreendata.booknow.domain.AvailableSlot;
import com.igreendata.booknow.response.AvailableSlotResponse;
import com.igreendata.booknow.service.ReservationsService;
import com.igreendata.booknow.util.PropertyCopier;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "/v1/availableSlots")
@Api(value = "Reservation Resource", description = "API for Reservation Resource")
@Slf4j
public class AvailableSlotController {

    @Autowired
    private ReservationsService reservationsService;

    @Autowired
    private PropertyCopier propertyCopier;

    @GetMapping
    @ApiOperation(value = "Get available slots for a given date")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 500, message = "Internal Server Error"),
                    @ApiResponse(code = 200, message = "Successful")
            }
    )
    public List<AvailableSlotResponse> findAllAvailableSlots(@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        List<AvailableSlotResponse> availableSlotResponses = new ArrayList<>();

        for (AvailableSlot availableSlot : reservationsService.findAllAvailableSlots(date)) {
            AvailableSlotResponse availableSlotResponse = new AvailableSlotResponse();
            propertyCopier.copyProperties(availableSlot, availableSlotResponse, Set.of("tableName", "availableDate", "availableTime"));

            availableSlotResponses.add(availableSlotResponse);
        }

        return availableSlotResponses;
    }


}
