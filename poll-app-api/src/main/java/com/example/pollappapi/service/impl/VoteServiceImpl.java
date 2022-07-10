package com.example.pollappapi.service.impl;

import com.example.pollappapi.model.Vote;
import com.example.pollappapi.repository.IVoteRepository;
import com.example.pollappapi.service.IVoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VoteServiceImpl implements IVoteService {

    @Autowired
    private IVoteRepository voteRepository;

    @Override
    public List<Vote> findAll() {
        return null;
    }

    @Override
    public Vote findById(Long id) {
        return null;
    }

    @Override
    public Vote save(Vote vote) {
        return null;
    }

    @Override
    public Vote update(Long id, Vote vote) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public long countByUserId(Long userId) {
        return voteRepository.countByUserId(userId);
    }
}
