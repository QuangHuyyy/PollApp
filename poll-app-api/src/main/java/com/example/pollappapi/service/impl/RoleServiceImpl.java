package com.example.pollappapi.service.impl;

import com.example.pollappapi.exception.AppException;
import com.example.pollappapi.model.ERoleName;
import com.example.pollappapi.model.Role;
import com.example.pollappapi.repository.IRoleRepository;
import com.example.pollappapi.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements IRoleService {
    @Autowired
    private IRoleRepository roleRepository;

    @Override
    public List<Role> findAll() {
        return null;
    }

    @Override
    public Role findById(Long id) {
        return null;
    }

    @Override
    public Role save(Role role) {
        return null;
    }

    @Override
    public Role update(Long id, Role role) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public Role findByName(ERoleName eRoleName) {
        return roleRepository.findByName(eRoleName).orElseThrow(() -> new AppException("User Role not set."));
    }
}
