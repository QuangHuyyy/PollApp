package com.example.pollappapi.repository;

import com.example.pollappapi.model.ERoleName;
import com.example.pollappapi.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERoleName eRoleName);
}
