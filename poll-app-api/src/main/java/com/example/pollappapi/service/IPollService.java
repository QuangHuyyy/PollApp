package com.example.pollappapi.service;

import com.example.pollappapi.model.Poll;
import com.example.pollappapi.payload.request.PollRequest;
import com.example.pollappapi.payload.request.VoteRequest;
import com.example.pollappapi.payload.response.PagedResponse;
import com.example.pollappapi.payload.response.PollResponse;
import com.example.pollappapi.security.UserPrincipal;

public interface IPollService {
    PagedResponse<PollResponse> getAllPolls(UserPrincipal currentUser, int page, int size);

    PagedResponse<PollResponse> getPollsCreatedBy(String username, UserPrincipal currentUser, int page, int size);

    PagedResponse<PollResponse> getPollsVotedBy(String username, UserPrincipal currentUser, int page, int size);

    Poll createPoll(PollRequest pollRequest);

    PollResponse getPollById(Long pollId, UserPrincipal currentUser);

    PollResponse castVoteAndGetUpdatedPoll(Long pollId, VoteRequest voteRequest, UserPrincipal currentUser);

    long countByCreatedBy(Long userId);

}
