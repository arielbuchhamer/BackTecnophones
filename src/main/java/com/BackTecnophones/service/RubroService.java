package com.BackTecnophones.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.BackTecnophones.model.Rubro;
import com.BackTecnophones.repository.RubroRepository;

@Service
@Transactional
public class RubroService implements GenericService<Rubro> {

	private final RubroRepository rubroRepo;
	
	public RubroService(RubroRepository rubroRepo) {
		this.rubroRepo = rubroRepo;
	}
	
	@Override
	public Optional<Rubro> findById(String id) {
		return rubroRepo.findById(id);
	}

	@Override
	public List<Rubro> findAll() {
		return rubroRepo.findAllByOrderByIdDesc();
	}

	@Override
	public Rubro save(Rubro entity) {
		return rubroRepo.save(entity);
	}

	@Override
	public void delete(String id) {
		rubroRepo.deleteById(id);
	}
	
	public Rubro updateRubro(String id, Rubro rubroNuevo) {
		return rubroRepo.findById(id).map(rubroExistente -> {
			rubroExistente.setDescripcion(rubroNuevo.getDescripcion());

			return rubroRepo.save(rubroExistente);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rubro no encontrado"));
	}

}
