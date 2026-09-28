package com.example.ddadang.domain.glucose.exception;

import com.example.ddadang.domain.glucose.status.CgmErrorStatus;

public class InvalidOAuthStateException extends RuntimeException {

    public InvalidOAuthStateException() {
        super(CgmErrorStatus.INVALID_OAUTH_STATE.getMessage());
    }
}
