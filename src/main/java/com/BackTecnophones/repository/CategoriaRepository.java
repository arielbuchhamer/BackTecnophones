package com.BackTecnophones.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.BackTecnophones.model.Categoria;

@Repository
public interface CategoriaRepository  extends MongoRepository<Categoria, String>{

}
