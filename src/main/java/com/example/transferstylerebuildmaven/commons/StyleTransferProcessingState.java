package com.example.transferstylerebuildmaven.commons;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class StyleTransferProcessingState {
    private RequestState state;
    private String description;
}
