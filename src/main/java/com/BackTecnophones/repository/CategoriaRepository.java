package com.BackTecnophones.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.BackTecnophones.model.Categoria;

@Repository
public interface CategoriaRepository  extends MongoRepository<Categoria, String>{
	List<Categoria> findByRubroId(String rubroId);
	List<Categoria> findAllByOrderByIdDesc();
}
