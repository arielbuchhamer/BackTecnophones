package com.BackTecnophones.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.BackTecnophones.model.facturacion.ComprobanteEmitido;

@Repository
public interface ComprobanteEmitidoRepository extends MongoRepository<ComprobanteEmitido, String> {
	Optional<ComprobanteEmitido> findByIdempotencyKey(String idempotencyKey);
}
