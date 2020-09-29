package com.igreendata.booknow.exception;

public class TimeConflictExistsException extends  RuntimeException {
    public TimeConflictExistsException() {
        super("Time conflict exists");
    }
}
