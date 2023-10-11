package com.example.transferstylerebuildmaven.respones.style_transfers;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;


@Getter
@Setter
@ToString
public class RequestStyleTransferResponse {
    private UUID uuidRequest;
    private String description;

    public RequestStyleTransferResponse(String description, UUID uuidRequest) {
        this.description = description;
        this.uuidRequest = uuidRequest;
    }
}
