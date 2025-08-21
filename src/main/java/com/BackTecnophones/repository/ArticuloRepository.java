package com.BackTecnophones.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.BackTecnophones.model.Articulo;

@Repository
public interface ArticuloRepository extends MongoRepository<Articulo, String>{

}
