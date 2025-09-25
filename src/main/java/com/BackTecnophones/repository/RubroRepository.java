package com.BackTecnophones.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.BackTecnophones.model.Rubro;

@Repository
public interface RubroRepository extends MongoRepository<Rubro, String> {
	List<Rubro> findAllByOrderByIdDesc();
}
