package com.BackTecnophones.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.BackTecnophones.model.Accesorio;
import com.BackTecnophones.repository.AccesorioRepository;

@Service
@Transactional
public class AccesorioService implements GenericService<Accesorio>
{
	private final AccesorioRepository accesorioRepo;
	
	public AccesorioService(AccesorioRepository accesorioRepo) {
		this.accesorioRepo = accesorioRepo;
	}
	
	@Override
	public Optional<Accesorio> findById(String id) {
		return accesorioRepo.findById(id);
	}

	@Override
	public List<Accesorio> findAll() {
		return accesorioRepo.findAll();
	}

	@Override
	public Accesorio save(Accesorio entity) {
		return accesorioRepo.save(entity);
	}

	@Override
	public void delete(String id) {
		accesorioRepo.deleteById(id);
	}
	
	public Accesorio updateAccesorio(String id, Accesorio accesorioNuevo) {
		return accesorioRepo.findById(id).map(accesorioExistente -> {
			accesorioExistente.setDescripcion(accesorioNuevo.getDescripcion());
			accesorioExistente.setPrecio(accesorioNuevo.getPrecio());
			
			return accesorioRepo.save(accesorioExistente);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Accesorio no encontrado"));
	}

}
