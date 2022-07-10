package com.example.pollappapi.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class ChoiceRequest {
    @NotBlank
    @Size(max = 50)
    private String text;
}
