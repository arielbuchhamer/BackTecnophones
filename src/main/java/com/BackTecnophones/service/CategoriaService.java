package com.BackTecnophones.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.BackTecnophones.model.Categoria;
import com.BackTecnophones.repository.CategoriaRepository;

@Service
@Transactional
public class CategoriaService implements GenericService<Categoria>{

	private final CategoriaRepository categoriaRepo;
	
	public CategoriaService(CategoriaRepository categoriaRepo) {
		this.categoriaRepo = categoriaRepo;
	}

	@Override
	public Optional<Categoria> findById(String id) {
		return categoriaRepo.findById(id);
	}

	@Override
	public List<Categoria> findAll() {
		return categoriaRepo.findAll();
	}

	@Override
	public Categoria save(Categoria entity) {
		return categoriaRepo.save(entity);
	}

	@Override
	public void delete(String id) {
		categoriaRepo.deleteById(id);		
	}
	
	public Categoria updateCategoria(String id, Categoria categoriaNueva)
	{
		return categoriaRepo.findById(id).map(categoriaExistente -> {
			categoriaExistente.setDescripcion(categoriaNueva.getDescripcion());
			categoriaExistente.setRubroId(categoriaNueva.getRubroId());

			return categoriaRepo.save(categoriaExistente);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada"));
	}
	
	public List<Categoria> getCategoriaByRubro(String rubroId) {
		List<Categoria> categorias = categoriaRepo.findByRubroId(rubroId);
		if (categorias.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"No se encontraron categorias para el rubro especificado");
		}
		return categorias;
	}
}
