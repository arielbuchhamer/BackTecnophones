package com.BackTecnophones.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.BackTecnophones.model.Usuario;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String>{
	Optional<Usuario> findByUserName(String username);
}
