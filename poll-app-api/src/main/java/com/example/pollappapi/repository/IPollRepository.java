package com.example.pollappapi.repository;

import com.example.pollappapi.model.Poll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPollRepository extends JpaRepository<Poll, Long> {
    //Optional<Poll> findById(Long id);
    Page<Poll> findByCreatedBy(Long userId, Pageable pageable);
    long countByCreatedBy(Long userId);
    List<Poll> findByIdIn(List<Long> pollIds);
    List<Poll> findByIdIn(List<Long> pollIds, Sort sort);
}
