package com.example.pollappapi.service;

import java.util.List;

public interface IGeneralService<T> {
    List<T> findAll();
    T findById(Long id);
    T save(T t);
    T update(Long id, T t);
    void delete(Long id);
}
