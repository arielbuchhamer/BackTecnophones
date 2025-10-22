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
	
	@Aggregation(pipeline = {
		    // Convierte strings a ObjectId; si falla, deja null (no truena el pipeline)
		    "{ $addFields: { " +
		      "rubroIdObj: { $convert: { input: \"$rubroId\", to: \"objectId\", onError: null, onNull: null } }, " +
		      "categoriaIdObj: { $convert: { input: \"$categoriaId\", to: \"objectId\", onError: null, onNull: null } } " +
		    "} }",
		    // Join con rubro y categoria
		    "{ $lookup: { from: \"rubros\", localField: \"rubroIdObj\", foreignField: \"_id\", as: \"rubro\" } }",
		    "{ $unwind: { path: \"$rubro\", preserveNullAndEmptyArrays: true } }",
		    "{ $lookup: { from: \"categorias\", localField: \"categoriaIdObj\", foreignField: \"_id\", as: \"categoria\" } }",
		    "{ $unwind: { path: \"$categoria\", preserveNullAndEmptyArrays: true } }",
		    // Proyección final: solo lo que necesitás
		    "{ $project: { " +
		      "_id: 1, descripcion: 1, stock: 1, precio: 1, variantes: 1, rubroId: 1, categoriaId: 1, imageId: 1 " +
		      "rubroDescripcion: { $ifNull: [ \"$rubro.descripcion\", \"\" ] }, " +
		      "categoriaDescripcion: { $ifNull: [ \"$categoria.descripcion\", \"\" ] } " +
		    "} }"
		  })
		  List<Articulo> findAllCompletos();
}
