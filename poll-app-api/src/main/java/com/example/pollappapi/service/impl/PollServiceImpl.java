package com.example.pollappapi.service.impl;

import com.example.pollappapi.exception.BadRequestException;
import com.example.pollappapi.exception.ResourceNotFoundException;
import com.example.pollappapi.model.*;
import com.example.pollappapi.payload.request.PollRequest;
import com.example.pollappapi.payload.request.VoteRequest;
import com.example.pollappapi.payload.response.PagedResponse;
import com.example.pollappapi.payload.response.PollResponse;
import com.example.pollappapi.repository.IPollRepository;
import com.example.pollappapi.repository.IUserRepository;
import com.example.pollappapi.repository.IVoteRepository;
import com.example.pollappapi.security.UserPrincipal;
import com.example.pollappapi.service.IPollService;
import com.example.pollappapi.util.AppConstants;
import com.example.pollappapi.util.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PollServiceImpl implements IPollService {
    @Autowired
    private IPollRepository pollRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IVoteRepository voteRepository;

    private static final Logger logger = LoggerFactory.getLogger(PollServiceImpl.class);

    @Override
    public PagedResponse<PollResponse> getAllPolls(UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        // Retrieve polls
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Poll> polls = pollRepository.findAll(pageable);

        if (polls.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), polls.getNumber(), polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
        }

        // Map Polls to PollResponses containing vote counts and poll creator details
        //List<Long> pollIds = polls.map(Poll::getId).getContent();
        List<Long> pollIds = new ArrayList<>(); // danh sách id của tất cả các poll
        for (Poll p : polls.getContent()) {
            pollIds.add(p.getId());
        }

        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds); // lựa chọn có bao nhiêu người chọn

        Map<Long, Long> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds); // lấy ra các votes do người dùng đã đăng nhập của các poll

        Map<Long, User> creatorMap = getPollCreatorMap(polls.getContent()); // lấy ra danh sách người tạo các poll của các poll ở page hiện tại

        List<PollResponse> pollResponses = polls.map(poll -> ModelMapper.mapPollToPollResponse(poll, choiceVoteCountMap,
                creatorMap.get(poll.getCreatedBy()),
                pollUserVoteMap.getOrDefault(poll.getId(), null))).getContent();

        return new PagedResponse<>(pollResponses, polls.getNumber(), polls.getSize(),
                polls.getNumberOfElements(), polls.getTotalPages(), polls.isLast());
    }

    @Override
    public PagedResponse<PollResponse> getPollsCreatedBy(String username, UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);
        validatePageNumberAndSize(page, size);

        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new ResourceNotFoundException("User", "username", username)
        );

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Poll> polls = pollRepository.findByCreatedBy(user.getId(), pageable);

        if (polls.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), polls.getNumber(), polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
        }

        List<Long> pollIds = polls.map(Poll::getId).getContent();
        Map<Long, Long> choiceVotesMap = getChoiceVoteCountMap(pollIds);
        Map<Long, Long> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);

        List<PollResponse> pollResponses = polls.map(poll -> ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, user,
                pollUserVoteMap.getOrDefault(poll.getId(), null))).getContent();
        return new PagedResponse<>(pollResponses, polls.getNumber(), polls.getSize(), polls.getTotalElements(),
                polls.getTotalPages(), polls.isLast());
    }

    @Override
    public PagedResponse<PollResponse> getPollsVotedBy(String username, UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new ResourceNotFoundException("User", "Username", username)
        );

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Long> userVotedPollIds = voteRepository.findVotedPollIdsByUserId(user.getId(), pageable);

        if(userVotedPollIds.getNumberOfElements() == 0){
            return new PagedResponse<>(Collections.emptyList(), userVotedPollIds.getNumber(), userVotedPollIds.getSize(), userVotedPollIds.getTotalElements(), userVotedPollIds.getTotalPages(), userVotedPollIds.isLast());
        }
        List<Long> pollIds = userVotedPollIds.getContent();
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        List<Poll> polls = pollRepository.findByIdIn(pollIds, sort);
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
        Map<Long, Long> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);
        Map<Long, User> creatorMap = getPollCreatorMap(polls);

        List<PollResponse> pollResponses = polls.stream().map(poll -> ModelMapper.mapPollToPollResponse(poll,
                choiceVoteCountMap,
                creatorMap.get(poll.getCreatedBy()),
                pollUserVoteMap.getOrDefault(poll.getId(), null))).collect(Collectors.toList());

        return new PagedResponse<>(pollResponses, userVotedPollIds.getNumber(), userVotedPollIds.getSize(), userVotedPollIds.getTotalElements(), userVotedPollIds.getTotalPages(), userVotedPollIds.isLast());
    }

    @Override
    public Poll createPoll(PollRequest pollRequest) {
        Poll poll = new Poll();
        poll.setQuestion(pollRequest.getQuestion());
        pollRequest.getChoices().forEach(choice -> {
            poll.addChoice(new Choice(choice.getText()));
        });

        Instant now = Instant.now();
        Instant expirationDateTime = now.plus(Duration.ofDays(pollRequest.getPollLength().getDays()))
                .plus(Duration.ofHours(pollRequest.getPollLength().getHours()));

        poll.setExpirationDateTime(expirationDateTime);

        return pollRepository.save(poll);
    }

    @Override
    public PollResponse getPollById(Long pollId, UserPrincipal currentUser) {
        Poll poll = pollRepository.findById(pollId).orElseThrow(() -> new ResourceNotFoundException("Poll", "Id", pollId));

        List<ChoiceVoteCount> voteCounts = voteRepository.countByPollIdGroupByChoiceId(pollId);

        Map<Long, Long> choiceVotesMap = voteCounts.stream().collect(Collectors.toMap(
                ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount
        ));

        User creator = userRepository.findById(poll.getCreatedBy()).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", poll.getCreatedBy())
        );

        Vote userVote = null;
        if (currentUser != null) {
            userVote = voteRepository.findByUserIdAndPollId(currentUser.getId(), pollId);
        }
        return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, creator,
                userVote == null ? null : userVote.getChoice().getId()
        );
    }

    @Override
    public PollResponse castVoteAndGetUpdatedPoll(Long pollId, VoteRequest voteRequest, UserPrincipal currentUser) {
        Poll poll = pollRepository.findById(pollId).orElseThrow(() -> new ResourceNotFoundException("Poll", "Id", pollId));

        if (poll.getExpirationDateTime().isBefore(Instant.now())) {
            throw new BadRequestException("Sorry! This Poll has already expired");
        }
        User user = userRepository.getById(currentUser.getId());

        Choice selectedChoice = poll.getChoices().stream()
                .filter(choice -> choice.getId().equals(voteRequest.getChoiceId()))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Choice", "id", voteRequest.getChoiceId()));

        Vote vote = new Vote();
        vote.setPoll(poll);
        vote.setChoice(selectedChoice);
        vote.setUser(user);

        try {
            vote = voteRepository.save(vote);
        } catch (DataIntegrityViolationException ex) {
            logger.info("User {} has already voted in Poll {}", currentUser.getId(), pollId);
            throw new BadRequestException("Sorry! You have already cast your vote in this poll");
        }

        // vote saved, return the update poll response

        List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);

        Map<Long, Long> choiceVotesMap = votes.stream().collect(
                Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount)
        );

        User creator = userRepository.findById(poll.getCreatedBy()).orElseThrow(
                () -> new ResourceNotFoundException("Poll", "Id", poll.getCreatedBy()));
        return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, creator, vote.getUser().getId());
    }

    @Override
    public long countByCreatedBy(Long userId) {
        return pollRepository.countByCreatedBy(userId);
    }


    private void validatePageNumberAndSize(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be less than zero!");
        }

        if (size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }

    private Map<Long, Long> getChoiceVoteCountMap(List<Long> pollIds) {
        List<ChoiceVoteCount> votes = voteRepository.countByPollIdInGroupByChoiceId(pollIds);

        Map<Long, Long> choiceVotesMap = new HashMap<>();
        for (ChoiceVoteCount i : votes) {
            choiceVotesMap.put(i.getChoiceId(), i.getVoteCount());
        }

        return choiceVotesMap;
    }

    private Map<Long, Long> getPollUserVoteMap(UserPrincipal currentUser, List<Long> pollIds) {
        Map<Long, Long> pollUserVoteMap = new HashMap<>();
        if (currentUser != null) {
            List<Vote> userVote = voteRepository.findByUserIdAndPollIdIn(currentUser.getId(), pollIds);

            for (Vote vote : userVote) {
                pollUserVoteMap.put(vote.getPoll().getId(), vote.getChoice().getId());
            }
        }

        return pollUserVoteMap;
    }

    private Map<Long, User> getPollCreatorMap(List<Poll> polls) {
        List<Long> creatorIds = new ArrayList<>();

        for (Poll p : polls) {
            creatorIds.add(p.getCreatedBy());
        }

        List<User> creators = userRepository.findByIdIn(creatorIds);

        Map<Long, User> creatorMap = new HashMap<>();
        for (User u : creators) {
            creatorMap.put(u.getId(), u);
        }

        return creatorMap;
    }
}
