package com.BackTecnophones.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
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
	private static final SecureRandom RNG = new SecureRandom();
    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	
	public ArticuloService(ArticuloRepository articuloRepo) {
		this.articuloRepo = articuloRepo;
	}
	
	@Override
	public Optional<Articulo> findById(String id) {
		return articuloRepo.findById(id);
	}

	@Override
	public List<Articulo> findAll() {
		return articuloRepo.findAllByOrderByIdDesc();
	}

	@Override
	public Articulo save(Articulo entity) {
		if (entity.getId() == null || entity.getId().isBlank()) 
			entity.setId(new ObjectId().toHexString());
	    
		if (entity.getVariantes() != null && !entity.getVariantes().isEmpty())
			entity.getVariantes().forEach(variante -> variante.setSku(entity.getId() + "-" + randomToken(6)));
		
		if (!entity.getVariantes().isEmpty())
			entity.setStock(entity.getVariantes().stream().mapToInt(v -> v.getStockVariante()).sum());
			
		return articuloRepo.save(entity);
	}
	
	private static String randomToken(int len) {
        char[] buf = new char[len];
        for (int i = 0; i < len; i++) {
            buf[i] = ALPHABET[RNG.nextInt(ALPHABET.length)];
        }
        return new String(buf);
    }

	@Override
	public void delete(String id) {
		articuloRepo.deleteById(id);
	}
	
	public Articulo updateArticulo(String id, Articulo articuloNuevo) {
		return articuloRepo.findById(id).map(articuloExistente -> {
			articuloExistente.setDescripcion(articuloNuevo.getDescripcion());
			articuloExistente.setPrecio(articuloNuevo.getPrecio());
			
			articuloExistente.setImageId(articuloNuevo.getImageId());
			articuloExistente.setRubroId(articuloNuevo.getRubroId());
			articuloExistente.setCategoriaId(articuloNuevo.getCategoriaId());
			
			if (!articuloNuevo.getVariantes().isEmpty())
			{
				articuloExistente.setVariantes(articuloNuevo.getVariantes());
				articuloExistente.setStock(articuloNuevo.getVariantes().stream().mapToInt(v -> v.getStockVariante()).sum());
			}
			else
			{
				articuloExistente.setStock(articuloNuevo.getStock());
			}
				

			return articuloRepo.save(articuloExistente);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Articulo no encontrado"));
	}
	
	public List<Articulo> getArticulosRandom(int limit) {
		int safeLimit = Math.max(1, Math.min(limit, 50));
		return articuloRepo.findArticulosRandom(safeLimit);
	}

}
