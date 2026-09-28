package com.example.ddadang.domain.glucose.exception;

import com.example.ddadang.domain.glucose.status.CgmErrorStatus;

public class InvalidEventTypeException extends RuntimeException {

    public InvalidEventTypeException() {
        super(CgmErrorStatus.INVALID_EVENT_TYPE.getMessage());
    }
}
