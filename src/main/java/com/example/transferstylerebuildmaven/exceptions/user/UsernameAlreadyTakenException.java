package com.example.transferstylerebuildmaven.exceptions.user;

public class UsernameAlreadyTakenException extends RuntimeException{
    public UsernameAlreadyTakenException(String message){
        super(message);
    }
}
