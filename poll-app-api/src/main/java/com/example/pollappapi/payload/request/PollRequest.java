package com.example.pollappapi.payload.request;

import com.example.pollappapi.model.Choice;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
public class PollRequest {
    @NotBlank
    @Size(max = 150)
    private String question;

    @NotNull
    @Size(min = 2, max = 6)
    @Valid
    private List<Choice> choices;

    @NotNull
    @Valid
    private PollLength pollLength;
}
