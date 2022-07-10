package com.example.pollappapi.controller;

import com.example.pollappapi.model.Poll;
import com.example.pollappapi.payload.request.PollRequest;
import com.example.pollappapi.payload.request.VoteRequest;
import com.example.pollappapi.payload.response.ApiResponse;
import com.example.pollappapi.payload.response.PagedResponse;
import com.example.pollappapi.payload.response.PollResponse;
import com.example.pollappapi.security.CurrentUser;
import com.example.pollappapi.security.UserPrincipal;
import com.example.pollappapi.service.impl.PollServiceImpl;
import com.example.pollappapi.util.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping(value = "/api/polls")
public class PollController {
    @Autowired
    private PollServiceImpl pollService;

    @GetMapping
    public PagedResponse<PollResponse> getPolls(@CurrentUser UserPrincipal currentUser,
                                                @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size){
        return pollService.getAllPolls(currentUser, page, size);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createPoll(@Valid @RequestBody PollRequest pollRequest){
        Poll poll = pollService.createPoll(pollRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{pollId}")
                .buildAndExpand(poll.getId()).toUri();
        return ResponseEntity.created(location).body(new ApiResponse(true, "Poll created successfully"));
    }

    @GetMapping(value = "/{pollId}")
    public PollResponse getPollById(@CurrentUser UserPrincipal currentUser, @PathVariable Long pollId){
        return pollService.getPollById(pollId, currentUser);
    }

    @PostMapping(value = "/{pollId}/votes")
    @PreAuthorize("hasRole('USER')")
    public PollResponse castVote(@CurrentUser UserPrincipal currentUser, @PathVariable Long pollId,
                                 @Valid @RequestBody VoteRequest voteRequest){
        return pollService.castVoteAndGetUpdatedPoll(pollId, voteRequest, currentUser);
    }
}
