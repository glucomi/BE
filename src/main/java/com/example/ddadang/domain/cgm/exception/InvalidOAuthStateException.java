package com.example.ddadang.domain.cgm.exception;

import com.example.ddadang.domain.cgm.status.CgmErrorStatus;

public class InvalidOAuthStateException extends RuntimeException {

    public InvalidOAuthStateException() {
        super(CgmErrorStatus.INVALID_OAUTH_STATE.getMessage());
    }
}
