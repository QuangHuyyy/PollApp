package com.example.pollappapi.service;

import com.example.pollappapi.model.User;

import java.util.Optional;

public interface IUserService extends IGeneralService<User>{
    Boolean checkEmailExist(String email);
    Boolean checkUsernameExist(String username);

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
}
