package com.example.pollappapi.service;

import com.example.pollappapi.model.Vote;

public interface IVoteService extends IGeneralService<Vote> {
    long countByUserId(Long userId);
}
