package com.BackTecnophones.service;

import java.util.List;
import java.util.Optional;

public interface GenericService<T> {
	Optional<T> findById(String id);
    List<T> findAll();
    T save(T entity);
    void delete(String id);
}
