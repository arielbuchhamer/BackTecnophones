package com.BackTecnophones.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.BackTecnophones.model.Articulo;

@Repository
public interface ArticuloRepository extends MongoRepository<Articulo, String>{
	List<Articulo> findAllByOrderByIdDesc();
	
	@Aggregation(pipeline = {
	        "{ $sample: { size: ?0 } }",
	        "{ $project: { _id: 1, descripcion: 1, precio: 1, imageId: 1 } }"
	    })
	List<Articulo> findArticulosRandom(int size);
	
	List<Articulo> findByRubroIdOrderByIdDesc(String rubroId);
}
