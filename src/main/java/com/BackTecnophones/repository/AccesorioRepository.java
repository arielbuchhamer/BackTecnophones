package com.BackTecnophones.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.BackTecnophones.model.Accesorio;

@Repository
public interface AccesorioRepository extends MongoRepository<Accesorio, String>{

}
