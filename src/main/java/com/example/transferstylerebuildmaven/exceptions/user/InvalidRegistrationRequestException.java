package com.example.transferstylerebuildmaven.exceptions.user;

public class InvalidRegistrationRequestException extends RuntimeException {
    public InvalidRegistrationRequestException(String message) {
        super(message);
    }
}