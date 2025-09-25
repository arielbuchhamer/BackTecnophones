package com.BackTecnophones.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.BackTecnophones.model.Venta;

@Repository
public interface VentaRepository extends MongoRepository<Venta, String>{
	Optional<Venta> findByPagoOrderId(String orderId);
    boolean existsByPagoPaymentId(String paymentId);
    List<Venta> findAllByOrderByIdDesc();
}
