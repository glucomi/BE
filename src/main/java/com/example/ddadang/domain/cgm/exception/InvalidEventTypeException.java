package com.example.ddadang.domain.cgm.exception;

import com.example.ddadang.domain.cgm.status.CgmErrorStatus;

public class InvalidEventTypeException extends RuntimeException {

    public InvalidEventTypeException() {
        super(CgmErrorStatus.INVALID_EVENT_TYPE.getMessage());
    }
}
