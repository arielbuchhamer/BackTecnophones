package com.BackTecnophones.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.BackTecnophones.model.Categoria;
import com.BackTecnophones.service.CategoriaService;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {
	@Autowired
	private CategoriaService categoriaService;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Categoria crearCategoria(@RequestBody Categoria categoria) {
		return categoriaService.save(categoria);
	}
	
	@GetMapping
	public List<Categoria> obtenerCategorias() {
		return categoriaService.findAll();
	}
	
	@GetMapping("/{id}")
	public Optional<Categoria> categoriaById(@PathVariable String id) {
		return categoriaService.findById(id);
	}
	
	@DeleteMapping("/{id}")
	public void eliminarCategoria(@PathVariable String id) {
		categoriaService.delete(id);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Categoria> actualizarCategoria(@PathVariable String id,
			@RequestBody Categoria nuevaCategoria) {
		Categoria categoriaActualizada = categoriaService.updateCategoria(id, nuevaCategoria);

		return ResponseEntity.ok(categoriaActualizada);
	}
	
	@GetMapping("/rubro/{rubroId}")
    public ResponseEntity<List<Categoria>> getCategoriasByRubro(@PathVariable String rubroId) {
        return ResponseEntity.ok(categoriaService.getCategoriaByRubro(rubroId));
    }
	
	@GetMapping("/conrubro")
	public List<Categoria> getCategoriasConRubro() {
		return categoriaService.getCategoriasWithRubro();
	}
}
