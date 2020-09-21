package com.igreendata.booknow.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class ReservationControllerTest {

    @Test
    public void test_statuscode_is_ok() {
        get("/v1/reservations/1").then().statusCode(200);
    }

    @Test
    public void test_specific_reservations() {
        get("/v1/reservations/1").then().body("name", equalTo("Ram Manohar"));
    }
}