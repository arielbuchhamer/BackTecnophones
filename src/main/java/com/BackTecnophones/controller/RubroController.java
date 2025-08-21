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
import org.springframework.web.server.ResponseStatusException;

import com.BackTecnophones.model.Rubro;
import com.BackTecnophones.service.RubroService;

@RestController
@RequestMapping("/rubros")
public class RubroController {
	@Autowired
	private RubroService rubroService;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Rubro crearRubro(@RequestBody Rubro rubro) {
		return rubroService.save(rubro);
	}
	
	@GetMapping
	public List<Rubro> obtenerRubros() {
		return rubroService.findAll();
	}
	
	@GetMapping("/{id}")
	public Rubro obtenerRubroPorId(@PathVariable String id) {
		return rubroService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rubro no encontrado"));
	}
	
	@DeleteMapping("/{id}")
	public void eliminarRubro(@PathVariable String id) {
		rubroService.delete(id);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Rubro> actualizarRubro(String id, @RequestBody Rubro rubroNuevo) {
		return ResponseEntity.ok(rubroService.updateRubro(id, rubroNuevo));
	}
}
