package com.example.pollappapi.controller;

import com.example.pollappapi.exception.ResourceNotFoundException;
import com.example.pollappapi.model.User;
import com.example.pollappapi.payload.response.*;
import com.example.pollappapi.security.CurrentUser;
import com.example.pollappapi.security.UserPrincipal;
import com.example.pollappapi.service.IPollService;
import com.example.pollappapi.service.IUserService;
import com.example.pollappapi.service.IVoteService;
import com.example.pollappapi.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api")
public class UserController {
    @Autowired
    private IUserService userService;

    @Autowired
    private IPollService pollService;

    @Autowired
    private IVoteService voteService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public UserSummary getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        return new UserSummary(currentUser.getId(), currentUser.getUsername(), currentUser.getName());
    }

    @GetMapping("/user/checkUsernameAvailability")
    public UserIdentityAvailability checkUsernameAvailability (@RequestParam(value = "username") String username){
        Boolean isAvailable = !userService.checkUsernameExist(username);
        return new UserIdentityAvailability(isAvailable);
    }

    @GetMapping("/user/checkEmailAvailability")
    public UserIdentityAvailability checkEmailAvailability(@RequestParam(value = "email") String email){
        Boolean isAvailable = !userService.checkEmailExist(email);
        return new UserIdentityAvailability(isAvailable);
    }

    @GetMapping("/users/{username}")
    public UserProfile getUserProfile(@PathVariable(value = "username") String username){
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        long pollCount = pollService.countByCreatedBy(user.getId());
        long voteCount = voteService.countByUserId(user.getId());
        return new UserProfile(user.getId(), user.getUsername(), user.getName(), user.getCreatedAt(), pollCount, voteCount);
    }

    @GetMapping("/users/{username}/polls")
    public PagedResponse<PollResponse> getAllPollsCreatedBy(@PathVariable(value = "username") String username,
                                                            @CurrentUser UserPrincipal currerntUser,
                                                            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size){
        return pollService.getPollsCreatedBy(username, currerntUser, page, size);
    }

    @GetMapping("/users/{username}/votes")
    public PagedResponse<PollResponse> getAllPollsVoteBy(@PathVariable(value = "username") String username,
                                                         @CurrentUser UserPrincipal currerntUser,
                                                         @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                         @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size){
        return pollService.getPollsVotedBy(username, currerntUser, page, size);
    }
}
