package com.BackTecnophones.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.BackTecnophones.model.Articulo;
import com.BackTecnophones.repository.ArticuloRepository;

@Service
@Transactional
public class ArticuloService implements GenericService<Articulo>{
	private final ArticuloRepository articuloRepo;
	
	public ArticuloService(ArticuloRepository articuloRepo) {
		this.articuloRepo = articuloRepo;
	}
	
	@Override
	public Optional<Articulo> findById(String id) {
		return articuloRepo.findById(id);
	}

	@Override
	public List<Articulo> findAll() {
		return articuloRepo.findAll();
	}

	@Override
	public Articulo save(Articulo entity) {
		return articuloRepo.save(entity);
	}

	@Override
	public void delete(String id) {
		articuloRepo.deleteById(id);
	}
	
	public Articulo updateArticulo(String id, Articulo articuloNuevo) {
		return articuloRepo.findById(id).map(articuloExistente -> {
			articuloExistente.setDescripcion(articuloNuevo.getDescripcion());
			articuloExistente.setPrecio(articuloNuevo.getPrecio());
			articuloExistente.setStock(articuloNuevo.getStock());
			articuloExistente.setImageId(articuloNuevo.getImageId());
			articuloExistente.setRubroId(articuloNuevo.getRubroId());
			articuloExistente.setCategoriaId(articuloNuevo.getCategoriaId());

			return articuloRepo.save(articuloExistente);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Articulo no encontrado"));
	}

}
