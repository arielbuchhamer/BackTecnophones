package com.BackTecnophones.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.BackTecnophones.model.Categoria;

@Repository
public interface CategoriaRepository  extends MongoRepository<Categoria, String>{
	List<Categoria> findByRubroId(String rubroId);
	
	List<Categoria> findAllByOrderByIdDesc();

	@Aggregation(pipeline = {
			  // convertir rubroId (String) → ObjectId
			  "{ $addFields: { rubroIdObj: { $convert: { input: \"$rubroId\", to: \"objectId\", onError: null, onNull: null } } } }",

			  // ahora sí matchea con _id (ObjectId) de 'rubros'
			  "{ $lookup: { from: \"rubros\", localField: \"rubroIdObj\", foreignField: \"_id\", as: \"rubro\" } }",
			  "{ $unwind: { path: \"$rubro\", preserveNullAndEmptyArrays: true } }",

			  "{ $project: { _id: 1, descripcion: 1, rubroId: 1, " +
			    "rubroDescripcion: { $ifNull: [ \"$rubro.descripcion\", \"\" ] } } }"
			})
			List<Categoria> findAllEntityWithRubroDescripcion();

}
