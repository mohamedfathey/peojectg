package com.ProjectGraduation.auth.exception;

public class OtpStillValidException extends RuntimeException {
    public OtpStillValidException(String message) {
        super(message);
    }
}